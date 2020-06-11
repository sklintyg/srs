package se.inera.intyg.srs.service

import com.google.common.collect.Lists
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis
import se.inera.intyg.srs.persistence.entity.PredictionPriority
import se.inera.intyg.srs.persistence.repository.PredictionPriorityRepository
import se.inera.intyg.srs.persistence.entity.PredictionQuestion
import se.inera.intyg.srs.persistence.entity.PredictionResponse
import se.inera.intyg.srs.persistence.repository.QuestionRepository
import se.inera.intyg.srs.persistence.repository.ResponseRepository

/**
 * Updates the questions and answers (variables and factors) used as input to prediction models from file
 */
@Component
class ModelVariablesFileUpdateService(@Value("\${model.variablesFile}") val variablesFile: String,
                                      @Value("\${model.variablesFileWithoutSubdiag}") val variablesFileWithoutSubdiag: String,
                                      @Value("\${recommendations.importMaxLines: 1000}") val importMaxLines: Int,
                                      val questionRepo: QuestionRepository,
                                      val responseRepo: ResponseRepository,
                                      val diagnosisRepo: DiagnosisRepository,
                                      val predictPrioRepo: PredictionPriorityRepository,
                                      val resourceLoader: ResourceLoader) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun doUpdate() {
        val modelVersion = extractModelVersion();
        log.info("Removing old questions and responses, modelVersion: $modelVersion")
        doRemoveOldQuestionsAndResponses(modelVersion)
        log.info("Updating questions and responses, modelVersion: $modelVersion")
        doUpdateQuestionsAndResponses(variablesFile, modelVersion, true)
        doUpdateQuestionsAndResponses(variablesFileWithoutSubdiag, modelVersion, false)
        log.info("Finished update of model variables, modelVersion: $modelVersion")
    }

    private fun doRemoveOldQuestionsAndResponses(modelVersion: String) {
        log.info("Deleting question priorities for model version $modelVersion")
        predictPrioRepo.deleteAll(predictPrioRepo.findByModelVersion(modelVersion))
    }

    private fun extractModelVersion(): String {
        val modelVersionPattern = "(\\d_\\d).xlsx".toRegex()
        val match = modelVersionPattern.find(variablesFile)
        val modelVersion = match!!.groupValues.get(1)!!.replace("_", ".");
        return modelVersion
    }

    data class Variable(
            val name: String,
            val type: String,
            val automaticSelectionDiagnosisCode: String?,
            val questionText: String?,
            val helpText: String?,
            val modelVersion: String
    )

    data class VariableFactorValue(
            val varName: String,
            val responseText: String,
            val responseId: String,
            val order: Int?,
            val isDefault: Boolean,
            val automaticSelectionDiagnosisCode: String?,
            val modelVersion: String
    )

    data class DiagnosisVariable(
            val diagnosisCode: String,
            val varName: String,
            val order: Int?,
            val resolution: Int,
            val modelVersion: String
    )

    private fun readVariables(workbook: XSSFWorkbook, modelVersion: String): List<Variable> {
        log.debug("Reading variables sheet")
        val sheet = workbook.getSheet("Kodning av variabelnamn")
        var readMoreRows = true
        var rowNumber = 1
        var unimportableRows = 0
        val variables:ArrayList<Variable> = arrayListOf();
        while (readMoreRows && unimportableRows < 4) {
            val row = sheet.getRow(rowNumber) ?: break
            val varName = row.getCell(0).stringCellValue
            if (varName.isNullOrBlank()) {
                unimportableRows++
                rowNumber++
                continue
            }
            val varType = row.getCell(1).stringCellValue
            val varAutomaticSelectionDiagnosisCode = row.getCell(2)?.stringCellValue
            val varQuestionText = row.getCell(3)?.stringCellValue
            val varHelpText = row.getCell(5)?.stringCellValue
            val variable = Variable(varName, varType, varAutomaticSelectionDiagnosisCode, varQuestionText, varHelpText, modelVersion)
            variables.add(variable)
            rowNumber++
        }
        log.info("Found ${variables.size} variables")
        return variables
    }
    private fun readVariableFactorValues(workbook: XSSFWorkbook, modelVersion:String): Map<String, List<VariableFactorValue>> {
        log.debug("Reading factor values sheet")
        val sheet = workbook.getSheet("Kodning av värden för factor")
        var readMoreRows = true
        var rowNumber = 1
        var unimportableRows = 0
        val responseMap:HashMap<String, ArrayList<VariableFactorValue>> = HashMap()
        while (readMoreRows && unimportableRows < 4) {
            val row = sheet.getRow(rowNumber) ?: break
            val varName = row.getCell(0).stringCellValue
            if (varName.isNullOrBlank()) {
                unimportableRows++
                rowNumber++
                continue
            }
            val responseText = row.getCell(1).stringCellValue
            val responseId = row.getCell(2).stringCellValue
            val order = row.getCell(3)?.numericCellValue?.toInt()
            val isDefaultTxt = row.getCell(4)?.stringCellValue
            val isDefault = isDefaultTxt != null && isDefaultTxt.equals("T")
            val automaticSelectionDiagnosisCode = row.getCell(5)?.stringCellValue

            val variableFactorValue =
                    VariableFactorValue(varName, responseText, responseId, order, isDefault, automaticSelectionDiagnosisCode, modelVersion)
            var list:ArrayList<VariableFactorValue>? = responseMap.get(varName)
            if (list==null) {
                list = Lists.newArrayList();
                responseMap.put(varName, list)
            }
            list?.add(variableFactorValue)
            rowNumber++
        }
        log.info("Found factor values for ${responseMap.size} variables")
        return responseMap
    }

    /**
     * Returns a map of variableName -> List of diagnosisVariable
     */
    private fun readDiagnosisVariables(workbook: XSSFWorkbook, modelVersion: String): Map<String, List<DiagnosisVariable>> {
        log.debug("Reading diagnosis variable combinations sheet")
        val sheet = workbook.getSheet("Variabler per diagnos")
        var readMoreRows = true
        var rowNumber = 1
        var unimportableRows = 0
        val responseMap:HashMap<String, ArrayList<DiagnosisVariable>> = HashMap()
        while (readMoreRows && unimportableRows < 4) {
            val row = sheet.getRow(rowNumber) ?: break
            val diagnosisCode = row.getCell(0).stringCellValue
            if (diagnosisCode.isNullOrBlank()) {
                unimportableRows++
                rowNumber++
                continue
            }
            var resolution = 3;
            val varName = row.getCell(1).stringCellValue
            var order = row.getCell(2)?.numericCellValue?.toInt()
            // We want to have subdiag_groups with us but make sure it is with order 0
            if (order == null && varName.isNotBlank() && varName.contains("_subdiag_group")) {
                order = 0
                resolution = 4; // if we have subdiag groups, the prediction is considering 4 characters of the diagnosis code
            }
            val diagnosisVariable = DiagnosisVariable(diagnosisCode, varName, order, resolution, modelVersion)
            log.debug("Did read DiagnosisVariable: ${diagnosisVariable}")
            var list:ArrayList<DiagnosisVariable>? = responseMap.get(diagnosisCode)
            if (list==null) {
                list = Lists.newArrayList();
                responseMap.put(diagnosisCode, list)
            }
            list?.add(diagnosisVariable)
            rowNumber++
        }
        log.info("Found ${responseMap.size} diagnoses")
        return responseMap
    }

    /**
     * Creates or updates a response alternative for a prediction question
     */
    private fun storeResponse(forSubdiags: Boolean, variableFactorValue: VariableFactorValue, predictionQuestion: PredictionQuestion): PredictionResponse {
        var response: PredictionResponse? = null
        responseRepo.findPredictionResponseByQuestionAndResponseAndModelVersionAndForSubdiagnosis(variableFactorValue.varName,
                variableFactorValue.responseId, variableFactorValue.modelVersion, forSubdiags) ?.let { existingResponse ->
            log.debug("Updating existing prediction response with question prediction id '${variableFactorValue.varName}' " +
                    "and response prediction id '${variableFactorValue.responseId}'")
            response = responseRepo.save(existingResponse.copy(
                    answer = variableFactorValue.responseText,
                    predictionId = variableFactorValue.responseId,
                    isDefault = variableFactorValue.isDefault,
                    priority = variableFactorValue.order,
                    question = predictionQuestion,
                    automaticSelectionDiagnosisCode = variableFactorValue.automaticSelectionDiagnosisCode
            ))
        } ?: run {
            log.debug("Creating prediction response with question prediction id '${variableFactorValue.varName}' " +
                    "and response prediction id '${variableFactorValue.responseId}'")
            response = responseRepo.save(PredictionResponse(variableFactorValue.responseText,
                variableFactorValue.responseId, variableFactorValue.isDefault, variableFactorValue.order,
                variableFactorValue.modelVersion, forSubdiags,
                predictionQuestion, variableFactorValue.automaticSelectionDiagnosisCode))
        }

        return response!!
    }

    /**
     * @param variable variable/question indata
     * @param factorValues factors/responses indata (for the given variable/question)
     */
    private fun storeQuestionWithAnswers(forSubdiags:Boolean, variable:Variable, factorValues: List<VariableFactorValue>): PredictionQuestion {
        var question: PredictionQuestion? = null
        // Check if we have an existing question with the same prediction id/variable name
        questionRepo.findByPredictionIdAndForSubdiagnosis(variable.name, forSubdiags)?.let { existingQuestion ->
            // if existing, then update
            log.debug("Updating existing question with question prediction id '${variable.name}'")
            question = questionRepo.save(existingQuestion.copy(
                    question = variable.questionText,
                    helpText = variable.helpText,
                    answers = factorValues.map { variableFactorValue ->
                        storeResponse(forSubdiags,variableFactorValue, existingQuestion)})
            )
        } ?: run {
            // if not existing (null) create a new one
            log.debug("Creating question with question prediction id '${variable.name}'")
            question = questionRepo.save(PredictionQuestion(variable.questionText, variable.helpText, variable.name,
                variable.modelVersion, forSubdiags))
            // ... and a number of possible responses
            question?.answers = factorValues.map { variableFactorValue ->
                storeResponse(forSubdiags, variableFactorValue, question!!)}
        }
        return question!!
    }

    private fun storePredictionPriority(forSubdiags:Boolean, diagnosisVariable: DiagnosisVariable,
                                                       variableQuestionMap: Map<String, PredictionQuestion>): PredictionPriority {
        log.debug("Storing prediction priority '${diagnosisVariable.order}' for question '${diagnosisVariable.varName}'")
        // Just create new ones here since we are always clearing priorities on each run
        return predictPrioRepo.save(PredictionPriority(diagnosisVariable.order!!, diagnosisVariable.modelVersion, forSubdiags,
                variableQuestionMap.getValue(diagnosisVariable.varName)))
    }

    /**
     * @param variableQuestionMap map from variable name to persisted PredictionQuestion
     */
    private fun storePredictionDiagnosis(forSubdiags: Boolean,
                                               diagnosisCode: String,
                                               diagnosisVariables: List<DiagnosisVariable>,
                                               variableQuestionMap: Map<String, PredictionQuestion>,
                                               modelVersion: String): PredictionDiagnosis {
        var diagnosis: PredictionDiagnosis? = null
        var resolution = (diagnosisVariables.maxBy { it.resolution })!!.resolution
        diagnosisRepo.findOneByDiagnosisIdAndModelVersionAndForSubdiagnosis(diagnosisCode, modelVersion, forSubdiags) ?. let { existingDiagnosis ->
            log.debug("Updating priorities on existing prediction diagnosis with diagnosis code '$diagnosisCode'")
            diagnosis = diagnosisRepo.save(existingDiagnosis.copy(
                    diagnosisId = diagnosisCode,
                    prevalence = 0.0,
                    resolution = resolution,
                    questions = diagnosisVariables
                            // Filtrera bort frågor som inte har någon order/priority samt de som inte har någon fråga i
                            // Questions-mappen (de sätts automatiskt och visas ej i GUI)
                            .filter { diagnosisVariable ->  diagnosisVariable.order != null
                                    && variableQuestionMap.containsKey (diagnosisVariable.varName)}
                            // Spara/koppla övriga med rätt prioritet till respektive variabels fråga
                            .map {diagnosisVariable ->
                                storePredictionPriority(forSubdiags, diagnosisVariable, variableQuestionMap)
                            }))
        } ?: run {
            log.debug("Creating new prediction diagnosis with diagnosis code '$diagnosisCode' with variables $diagnosisVariables")
            // Prevalensen uppdateras i senare steg, vid inläsning av åtgärdsrekommendationer och prevalens
            diagnosis = diagnosisRepo.save(PredictionDiagnosis(diagnosisCode, 0.0, resolution, modelVersion, forSubdiags,
                    // För varje kombination av diagnos och variabel (fråga)
                    diagnosisVariables
                            // Filtrera bort frågor som inte har någon order/priority samt de som inte har någon fråga i
                            // Questions-mappen (de sätts automatiskt och visas ej i GUI)
                            .filter { diagnosisVariable ->
                                diagnosisVariable.order != null
                                        && variableQuestionMap.containsKey(diagnosisVariable.varName)
                            }
                            // Spara/koppla övriga med rätt prioritet till respektive variabels fråga
                            .map { diagnosisVariable ->
                                storePredictionPriority(forSubdiags, diagnosisVariable, variableQuestionMap)
                            }
            ))
        }
        return diagnosis!!
    }

    private fun doUpdateQuestionsAndResponses(file:String, modelVersion: String, forSubdiags:Boolean=true) {
        log.info("Performing update of model variables (questions and responses) for model version $modelVersion from file $file")
        val excelFileStream = resourceLoader.getResource(file).inputStream
        XSSFWorkbook(excelFileStream).use { workbook ->

            val variables = readVariables(workbook, modelVersion) // map of variableName -> variable (question)
            val variableFactorValues = readVariableFactorValues(workbook, modelVersion) // map of variableName -> response list
            val variableDiagnoses = readDiagnosisVariables(workbook, modelVersion) // map of diagnosis -> diagnosis variable list (for the variable)

            log.debug("Persisting questions and responses")
            // För varje modellvariabel (fråga)
            val variableQuestionMap = variables
                    .filter { variable ->
                        // ... som har typen "factor" ...
                        "factor".equals(variable.type) &&
                        // ... och minst ett svar med svarstext eller automatisk ifyllnad
                            variableFactorValues.getValue(variable.name)
                                .find { factor -> factor.responseText.isNotBlank() || !factor.automaticSelectionDiagnosisCode.isNullOrBlank() } != null
                    }
                    .map { variable ->
                        // ... skapa en fråga (och mappa till variabelnamn), från variabel och faktorer med text
                        variable.name to storeQuestionWithAnswers(forSubdiags, variable,
                                // Ta bara med factors/frågor som har svarstext eller sätts automatiskt mha diagnoskod,
                                // övriga (ålder, region etc) sätts automatiskt/hårdkodat och syns ej i GUI
                                variableFactorValues.getValue(variable.name).filter { factor -> factor.responseText.isNotBlank()
                                        || !factor.automaticSelectionDiagnosisCode.isNullOrBlank() }
                        )
                    }.toMap()

            log.debug("Persisting question diagnoses and question priorities")
            // För varje diagnoskod
            variableDiagnoses.forEach { diagnosisCode, diagnosisVariables ->
                // ... skapa en prediktions-diagnos
                storePredictionDiagnosis(forSubdiags, diagnosisCode, diagnosisVariables, variableQuestionMap, modelVersion)
            }

        }
    }

}
