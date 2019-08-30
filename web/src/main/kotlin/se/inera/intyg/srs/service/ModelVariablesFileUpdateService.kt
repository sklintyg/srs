package se.inera.intyg.srs.service

import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
                                      @Value("\${recommendations.importMaxLines: 1000}") val importMaxLines: Int,
                                      val questionRepo: QuestionRepository,
                                      val responseRepo: ResponseRepository,
                                      val diagnosisRepo: DiagnosisRepository,
                                      val predictPrioRepo: PredictionPriorityRepository,
                                      val resourceLoader: ResourceLoader) {

    private val log = LogManager.getLogger()

    final fun doUpdate() {
        log.info("Removing old questions and responses")
        doRemoveOldQuestionsAndResponses()
        log.info("Updating questions and responses")
        doUpdateQuestionsAndResponses()
    }

    private final fun doRemoveOldQuestionsAndResponses() {
        log.info("Deleting question priorities")
        predictPrioRepo.deleteAll()
    }

    data class Variable(
            val name: String,
            val type: String,
            val automaticSelectionDiagnosisCode: String?,
            val questionText: String?,
            val helpText: String?
    )

    data class VariableFactorValue(
            val varName: String,
            val responseText: String,
            val responseId: String,
            val order: Int?,
            val isDefault: Boolean
    )

    data class DiagnosisVariable(
            val diagnosisCode: String,
            val varName: String,
            val order: Int?
    )

    private final fun readVariables(workbook: XSSFWorkbook): List<Variable> {
        log.info("Reading variables sheet")
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
            val variable = Variable(varName, varType, varAutomaticSelectionDiagnosisCode, varQuestionText, varHelpText)
            variables.add(variable)
            rowNumber++
        }
        log.info("Found ${variables.size} variables")
        return variables
    }
    private final fun readVariableFactorValues(workbook: XSSFWorkbook): Map<String, List<VariableFactorValue>> {
        log.info("Reading factor values sheet")
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
            val variableFactorValue = VariableFactorValue(varName, responseText, responseId, order, isDefault)
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
    private final fun readDiagnosisVariables(workbook: XSSFWorkbook): Map<String, List<DiagnosisVariable>> {
        log.info("Reading diagnosis variable combinations sheet")
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
            val varName = row.getCell(1).stringCellValue
            val order = row.getCell(2)?.numericCellValue?.toInt()
            val diagnosisVariable = DiagnosisVariable(diagnosisCode, varName, order)
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

    private final fun storeResponse(variableFactorValue: VariableFactorValue, predictionQuestion: PredictionQuestion): PredictionResponse {
        var response: PredictionResponse? = null
        responseRepo.findPredictionResponseByQuestionAndResponse(variableFactorValue.varName,
                variableFactorValue.responseId) ?.let { existingResponse ->
            log.info("Updating existing prediction response with question prediction id '${variableFactorValue.varName}' " +
                    "and response prediction id '${variableFactorValue.responseId}'")
            response = responseRepo.save(existingResponse.copy(
                    answer = variableFactorValue.responseText,
                    predictionId = variableFactorValue.responseId,
                    isDefault = variableFactorValue.isDefault,
                    priority = variableFactorValue.order!!,
                    question = predictionQuestion
            ))
        } ?: run {
            log.info("Creating prediction response with question prediction id '${variableFactorValue.varName}' " +
                    "and response prediction id '${variableFactorValue.responseId}'")
            response = responseRepo.save(PredictionResponse(variableFactorValue.responseText,
                    variableFactorValue.responseId, variableFactorValue.isDefault, variableFactorValue.order!!, predictionQuestion))
        }
        return response!!
    }

    /**
     * @param variable variable/question indata
     * @param factorValues factors/responses indata (for the given variable/question)
     */
    private final fun storeQuestionWithAnswers(variable:Variable, factorValues: List<VariableFactorValue>): PredictionQuestion {
        var question: PredictionQuestion? = null
        // Check if we have an existing question with the same prediction id/variable name
        questionRepo.findByPredictionId(variable.name)?.let { existingQuestion ->
            // if existing, then update
            log.info("Updating existing question with question prediction id '${variable.name}'")
            question = questionRepo.save(existingQuestion.copy(
                    question = variable.questionText!!,
                    helpText = variable.helpText!!,
                    answers = factorValues.map { variableFactorValue ->
                        storeResponse(variableFactorValue, question!!)})
            )
        } ?: run {
            // if not existing (null) create a new one
            log.info("Creating question with question prediction id '${variable.name}'")
            question = questionRepo.save(PredictionQuestion(variable.questionText!!, variable.helpText!!, variable.name))
            // ... and a number of possible responses
            question?.answers = factorValues.map { variableFactorValue ->
                storeResponse(variableFactorValue, question!!)}
        }
        return question!!
    }

    private final fun storePredictionPriority(diagnosisVariable: DiagnosisVariable,
                                                       variableQuestionMap: Map<String, PredictionQuestion>): PredictionPriority {
        log.info("Storing prediction priority '${diagnosisVariable.order}' for question '${diagnosisVariable.varName}'")
        // Just create new ones here since we are always clearing priorities on each run
        return predictPrioRepo.save(PredictionPriority(diagnosisVariable.order!!,
                variableQuestionMap.getValue(diagnosisVariable.varName)))
    }

    /**
     * @param variableQuestionMap map from variable name to persisted PredictionQuestion
     */
    private final fun storePredictionDiagnosis(diagnosisCode: String,
                                               diagnosisVariables: List<DiagnosisVariable>,
                                               variableQuestionMap: Map<String, PredictionQuestion>): PredictionDiagnosis {
        var diagnosis: PredictionDiagnosis? = null
        diagnosisRepo.findOneByDiagnosisId(diagnosisCode) ?. let { existingDiagnosis ->
            log.info("Updating priorities on existing prediction diagnosis with diagnosis code '$diagnosisCode'")
            diagnosis = diagnosisRepo.save(existingDiagnosis.copy(
                    diagnosisId = diagnosisCode,
                    prevalence = 0.0,
                    questions = diagnosisVariables
                            // Filtrera bort frågor som inte har någon order/priority samt de som inte har någon fråga i
                            // Questions-mappen (de sätts automatiskt och visas ej i GUI)
                            .filter { diagnosisVariable ->  diagnosisVariable.order != null
                                    && variableQuestionMap.containsKey (diagnosisVariable.varName)}
                            // Spara/koppla övriga med rätt prioritet till respektive variabels fråga
                            .map {diagnosisVariable ->
                                storePredictionPriority(diagnosisVariable, variableQuestionMap)
                            }))
        } ?: run {
            log.info("Creating new prediction diagnosis with diagnosis code '$diagnosisCode' with variables $diagnosisVariables")
            // Prevalensen uppdateras i senare steg, vid inläsning av åtgärdsrekommendationer och prevalens
            diagnosis = diagnosisRepo.save(PredictionDiagnosis(diagnosisCode, 0.0,
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
                                storePredictionPriority(diagnosisVariable, variableQuestionMap)
                            }
            ))
        }
        return diagnosis!!
    }

    private final fun doUpdateQuestionsAndResponses() {
        log.info("Performing update of model variables (questions and responses) from file $variablesFile")
        val excelFileStream = resourceLoader.getResource(variablesFile).inputStream
        XSSFWorkbook(excelFileStream).use { workbook ->

            val variables = readVariables(workbook) // map of variableName -> variable (question)
            val variableFactorValues = readVariableFactorValues(workbook) // map of variableName -> response list
            val variableDiagnoses = readDiagnosisVariables(workbook) // map of diagnosis -> diagnosis variable list (for the variable)

            log.info("Persisting questions and responses")
            // För varje modellvariabel (fråga)
            val variableQuestionMap = variables
                    .filter { variable ->
                        // ... som har typen "factor" ...
                        "factor".equals(variable.type) &&
                        // ... och minst ett svar med svarstext
                            variableFactorValues.getValue(variable.name)
                                .find { factor -> factor.responseText.isNotBlank() } != null
                    }
                    .map { variable ->
                        // ... skapa en fråga (och mappa till variabelnamn), från variabel och faktorer med text
                        variable.name to storeQuestionWithAnswers(variable,
                                // Ta bara med factors/frågor som har svarstext, övriga sätts automatiskt och syns ej i GUI
                                variableFactorValues.getValue(variable.name).filter { factor -> factor.responseText.isNotBlank() }
                        )
                    }.toMap()

            log.info("Persisting question diagnoses and question priorities")
            // För varje diagnoskod
            variableDiagnoses.forEach { diagnosisCode, diagnosisVariables ->
                // ... skapa en prediktions-diagnos
                storePredictionDiagnosis(diagnosisCode, diagnosisVariables, variableQuestionMap)
            }

        }
    }

}
