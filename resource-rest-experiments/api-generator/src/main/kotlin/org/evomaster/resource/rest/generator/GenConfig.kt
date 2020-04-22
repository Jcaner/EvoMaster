package org.evomaster.resource.rest.generator

import org.evomaster.resource.rest.generator.GenerateREST.Companion.DEFAULT_PROPERTY_VALUE
import org.evomaster.resource.rest.generator.implementation.java.dependency.ConditionalDependencyKind
import org.evomaster.resource.rest.generator.model.CommonTypes
import org.evomaster.resource.rest.generator.model.GraphExportFormat
import org.evomaster.resource.rest.generator.model.RestMethod
import org.evomaster.resource.rest.generator.model.StrategyNameResource

/**
 * created by manzh on 2019-08-14
 */
class GenConfig {

    companion object{
        const val targetFile = "targets"
    }

    /**
     * output folder
     */
    var outputFolder = "/Users/mazh001/Documents/GitHub/automated-generated-api/"

    var parents = listOf("")

    var outputType = OutputType.MAVEN_MODULE

    var saveGraph = GraphExportFormat.DOT

    enum class OutputType{
        SOURCE,
        MAVEN_MODULE,
        MAVEN_PROJECT,
    }
    var outputContent = OutputContent.CS

    enum class OutputContent(val numOfModule : Int){
        //EM(1),
        CS(1),
        //EX(1),
        CS_EM(2),
        CS_EX(2),
        CS_EM_EX(3)
    }

    var groupId = "resource.exp"
    var projectName = "auto-rest-example"

    var csName = "cs"
    var emName = "em"
    var emMainClass = "EmbeddedEvoMasterController"

    var exName = "ex"
    var exMainClass = "ExternalEvoMasterController"

    var srcFolder = "src/main"

    var csProjectPackage = "org.autogenerated.rest.artificial.cs"

    var emProjectPackage = "em.resource.exp.artificial.controller"

    var exProjectPackage = "ex.resource.exp.artificial.controller"
    
    var language = Format.JAVA_SPRING_SWAGGER

    enum class Format(val srcFolder : String, val resource : String?){
        JAVA_SPRING_SWAGGER("java", "resources")
    }

    var restMethods = listOf(RestMethod.POST_ID, RestMethod.GET_ID, RestMethod.GET_ALL, RestMethod.PUT, RestMethod.DELETE, RestMethod.DELETE_CON ,RestMethod.PATCH_VALUE)

    var numOfNodes = 10

    var numOfOneToOne = 0

    var numOfOneToTwo = 0

    var numOfOneToMany = 0

    var numOfTwoToOne = 0

    var numOfManyToOne = 0

    var numOfTwoToTwo = 0

    var numOfManyToMany = 0

    var numOfExtraProperties = -1

    var numOfImpactProperties = 2

    var propertiesTypes = listOf(CommonTypes.OBJ_INT)//CommonTypes.values(); make request param nullable for patch method

    var branchesForImpact = 4

    var nameStrategy : StrategyNameResource = StrategyNameResource.RAND

    var dependencyKind : ConditionalDependencyKind = ConditionalDependencyKind.EXISTENCE

    var hideExistsDependency = false

    var dependencyProperty : String = DEFAULT_PROPERTY_VALUE

    var idName = "id"
    var idType = CommonTypes.OBJ_INT

    fun getCsOutputFolder() = "${FormatUtil.formatFolder(getCsRootFolder())}$srcFolder/${language.srcFolder}"
    fun getCsResourceFolder() = "${FormatUtil.formatFolder(getCsRootFolder())}$srcFolder/${language.resource}"

    fun getProjectFolder() = "${FormatUtil.formatFolder(outputFolder)}${if (outputContent.numOfModule > 1) "$projectName" else ""}"
    fun getCsRootFolder() = "${FormatUtil.formatFolder(getProjectFolder())}${if (outputType != OutputType.SOURCE) csName else ""}"

    fun getEmRootFolder() = "${FormatUtil.formatFolder(getProjectFolder())}${if (outputType != OutputType.SOURCE) emName else ""}"
    fun getExRootFolder() = "${FormatUtil.formatFolder(getProjectFolder())}${if (outputType != OutputType.SOURCE) exName else ""}"

    fun getEmOutputFolder() = "${FormatUtil.formatFolder(getProjectFolder())}$emName/$srcFolder/${language.srcFolder}"
    fun getEmResourceFolder() = "${FormatUtil.formatFolder(getProjectFolder())}$emName/$srcFolder/${language.resource}"

    fun getExOutputFolder() = "${FormatUtil.formatFolder(getProjectFolder())}$exName/$srcFolder/${language.srcFolder}"
    fun getExResourceFolder() = "${FormatUtil.formatFolder(getProjectFolder())}$exName/$srcFolder/${language.resource}"

    fun getFullExMainClass() = "$exProjectPackage.$exMainClass"

    fun repackageName() = "${projectName.toLowerCase()}"
    fun getCSJarName() = "${repackageName()}-sut"
    fun getEXJarFinalName() = "${repackageName()}-runner"

    fun getCSRootProjectPathForExternal() : String{
        if (outputContent != OutputContent.CS_EM_EX && outputContent != OutputContent.CS_EX)
            throw IllegalStateException("incorrect invocation")
        val p = parents.plus(projectName).drop(1).joinToString("/"){it}
        return if (p.isBlank()) p else "$p/"
    }

}