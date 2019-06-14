package se.inera.intyg.srs.service

import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.PredictionPriority
import se.inera.intyg.srs.persistence.PredictionPriorityRepository
import se.inera.intyg.srs.persistence.PredictionQuestion
import se.inera.intyg.srs.persistence.PredictionResponse
import se.inera.intyg.srs.persistence.QuestionRepository
import se.inera.intyg.srs.persistence.ResponseRepository

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
        doRemoveOldQuestionsAndResponses()
        doUpdateQuestionsAndResponses()
    }

    private final fun doRemoveOldQuestionsAndResponses() {
        responseRepo.deleteAll()
        questionRepo.deleteAll()
    }

    class Variable(
            val name: String,
            val type: String,
            val automaticSelectionDiagnosisCode: String?,
            val questionText: String?,
            val helpText: String?
    )

    class VariableFactorValue(
            val varName: String,
            val responseText: String,
            val responseId: String,
            val order: Int?,
            val isDefault: Boolean
    )

    class DiagnosisVariable(
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
        var rowNumber = 2
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

    private final fun storeResponse(variableFactorValue: VariableFactorValue): PredictionResponse {
        log.info("Storing response for factor: ${variableFactorValue.responseId}")
        return responseRepo.save(PredictionResponse(variableFactorValue.responseText,
                variableFactorValue.responseId, variableFactorValue.isDefault, variableFactorValue.order!!))
    }

    /**
     * @param variable variable/question indata
     * @param factorValues factors/responses indata (for the given variable/question)
     */
    private final fun storeQuestionWithAnswers(variable:Variable, factorValues: List<VariableFactorValue>): PredictionQuestion {
        log.info("Storing question for variable: ${variable.name}")
        return questionRepo.save(PredictionQuestion(variable.questionText!!, variable.helpText!!, variable.name,
                // ... och ett antal svar
                factorValues.map { variableFactorValue ->
                    storeResponse(variableFactorValue)
                }))
    }

    /**
     * @param variableQuestionMap map from variable name to persisted PredictionQuestion
     */
    private final fun storePredictionDiagnosis(diagnosisCode: String,
                                               diagnosisVariables: List<DiagnosisVariable>,
                                               variableQuestionMap: Map<String, PredictionQuestion>): PredictionDiagnosis {
        log.info("Storing prediciton diagnosis for diagnosis code: ${diagnosisCode}")
        // Prevalensen uppdateras i senare steg, vid inläsning av åtgärdsrekommendationer och prevalens
        return diagnosisRepo.save(PredictionDiagnosis(diagnosisCode, 0.0,
                // För varje kombination av diagnos och variabel (fråga)
                diagnosisVariables
                        // Filtrera bort frågor som inte har någon order/priority (de sätts automatiskt och visas ej i GUI
                        .filter { diagnosisVariable ->  diagnosisVariable.order != null}
                        // Spara/koppla övriga med rätt prioritet till respektive variabels fråga
                        .map {diagnosisVariable ->
                            predictPrioRepo.save(PredictionPriority(diagnosisVariable.order!!,
                                    variableQuestionMap.getValue(diagnosisVariable.varName)))
                }
        ))
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
