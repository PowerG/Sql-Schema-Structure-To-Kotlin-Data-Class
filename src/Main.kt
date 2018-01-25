import java.io.File

val columnAnnotation : String = "@Column"
var tableName = ""
var dataTypeFromSql = ""
var indexOfApostrpoheAfterTableName = 0
var indexAfterDataType = 0
var kotlinDataType = ""
var kotlinDataTypeDefaultValue = ""
var indexOfSpaceAfterDataType = 0
var canBeNull: Boolean = false;

fun main(args: Array<String>) {
    var dataClassArrayList : ArrayList<String>
    getFile()



}

fun getFile() {
    //TODO
    File("src/sql_table_structure.txt").forEachLine {
//        println(it)
        buildString(it)
    }

}

fun buildString(stringFromFile: String){
    //create list out of each character in the string
    var charListOfStringFromFile : ArrayList<String> = ArrayList()
    for (s in stringFromFile) {
        if(!s.equals(" ")){//remove spaces
            charListOfStringFromFile.add(s.toString())

        }
    }

    //reinitialise these global vars for the next row entry from the file we''e reading
    tableName = ""
    dataTypeFromSql = ""
    indexOfApostrpoheAfterTableName = 0
    kotlinDataType = ""
    indexOfSpaceAfterDataType = 0
    canBeNull = false


    var string : String = ""
    //add @column
    string +=columnAnnotation

    //add (name ="
    string +="""(name=""""

    //set and add table name
    tableName = getTableName(charListOfStringFromFile)
    string+=tableName

    //add ") val
    string+= """") val """

    //add variable name
    string+=underScoreToCamelCase(tableName)

    //add :
    string+=": "

    //add dataType
    string+=getKotlinDataType(getDataTypeFromSql(charListOfStringFromFile,indexOfApostrpoheAfterTableName))

    //add default value
    string+= "= $kotlinDataTypeDefaultValue"

    //add the last comma
    string+= ","

    //add "@NotBlank " as postfix if sql schema has "NOT NULL"
    checkIfColumnCanBeNull(charListOfStringFromFile,getIndexOfSpaceAfterDataType(charListOfStringFromFile,indexAfterDataType))

    var finalString : String
    finalString = getNullableString(canBeNull)+string

    println(finalString)


}



fun getTableName(charListOfStringFromFile: ArrayList<String>): String {
    var i = 1
    for (string in charListOfStringFromFile){
        var s = charListOfStringFromFile.get(i)
        if (s.equals("`")){
            indexOfApostrpoheAfterTableName = i
            break
        }
        tableName+=s
        i++
    }

    return tableName
}


fun underScoreToCamelCase(underScoreText: String): String {
    var camelCaseStringArray : ArrayList<String> = ArrayList()
    var previousCharWasAnUnderscore = false
    for (s in underScoreText) {
        if(!s.toString().equals("_")){
            if(previousCharWasAnUnderscore){
                camelCaseStringArray.add(s.toString().toUpperCase())
                previousCharWasAnUnderscore = false
            }else{
                camelCaseStringArray.add(s.toString())
            }
        }else{
            previousCharWasAnUnderscore = true
        }
    }

    return camelCaseStringArray.joinToString("")

}

fun getDataTypeFromSql(charListOfStringFromFile: ArrayList<String>, indexOfApostrpoheAfterTableName: Int): String {
    var i = indexOfApostrpoheAfterTableName+2 //account for the space
    for (string in charListOfStringFromFile){
        var s = charListOfStringFromFile.get(i)
        if (s.equals("(") || s.equals((" "))){
            indexAfterDataType =i
            break //the space is for when the data type deosn't have lenght e.g (11) on it
        }
        dataTypeFromSql+=s
        i++
    }

    return dataTypeFromSql
}

fun getIndexOfSpaceAfterDataType(charListOfStringFromFile: ArrayList<String>, indexAfterDataType: Int): Int{
    var i = indexAfterDataType

    if(charListOfStringFromFile.get(indexAfterDataType).equals(" ")){
        indexOfSpaceAfterDataType = indexAfterDataType
    }
    else if (charListOfStringFromFile.get(indexAfterDataType).equals("(")){
        for (string in charListOfStringFromFile){
            var s = charListOfStringFromFile.get(i)
            if (s.equals(" ")){
                indexOfSpaceAfterDataType =i
                break //the space is for when the data type deosn't have lenght e.g (11) on it
            }
            i++
        }
    }
    else{
        println("ERROR! Data is poorly formatted")
    }

    return  indexOfSpaceAfterDataType

}

fun getKotlinDataType(dataTypeFromSql: String): String {

    when (dataTypeFromSql){
        "int" -> {
            kotlinDataType = "Int"
            kotlinDataTypeDefaultValue = "0"
        }
        "varchar" -> {
            kotlinDataType = "String"
            kotlinDataTypeDefaultValue = """" """"
        }
        "bigint" -> {
            kotlinDataType = "BigInt"
            kotlinDataTypeDefaultValue = "0"
        }
        "float" -> {
            kotlinDataType = "Float"
            kotlinDataTypeDefaultValue = "0.0f"
        }
        "timestamp" -> { //TODO: Remember to insert a default value for each entry in each sql row entry before running this script. e.g timestamp (0) NULL" instead of "timestamp NULL"
            kotlinDataType = "Date"
            kotlinDataTypeDefaultValue = "Date()"
        }
        else ->{
            print("Data type not defined!!")
        }
    }

    return kotlinDataType
}

fun checkIfColumnCanBeNull(charListOfStringFromFile: ArrayList<String>, indexOfSpaceAfterDataType: Int) {
    //the next text after this "space" will either be "NOT NULL", "DEFAULT NULL" or "NULL"
    //get first 3 characters and check (becuase the word NOT is 3 characters, and I somehow prefer 3 to 2)

    var i = indexOfSpaceAfterDataType+1 //ignore the space
    var indexWhereToStop = i+3
    var nullOrNotNullStringArray : ArrayList<String> = ArrayList(0)
    for (string in charListOfStringFromFile){
        if (i==indexWhereToStop){
            break
        }
        var s = charListOfStringFromFile.get(i)
        nullOrNotNullStringArray.add(s)
        i++
    }

    when (nullOrNotNullStringArray.joinToString(separator = "")){
        "NOT" ->{
            canBeNull = false
        }
        "DEF" ->{
            canBeNull = true
        }
        "NUL" ->{
            canBeNull = true
        }
        else-> {
        println("Can't tell whether this is nullable or not")
    }
    }
}

fun getNullableString(canBeNull: Boolean) :String{
    var nullabeString = ""

    if(!canBeNull){
        nullabeString = "@NotBlank"
    }

    return nullabeString

}

