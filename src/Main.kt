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
var firstLineHasBeenRead: Boolean = false;

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
    string +="""(name = """"

    //set and add table name
    tableName = getTableName(charListOfStringFromFile)
    string+=tableName

    //add ") val
    string+= """") val """

    //add variable name
    string+=underScoreToCamelCase(tableName)

    //add :
    string+=" : "

    //add dataType
    string+=getKotlinDataType(getDataTypeFromSql(charListOfStringFromFile,indexOfApostrpoheAfterTableName))

    //add default value
    string+= " = $kotlinDataTypeDefaultValue"

    //add the last comma
    string+= ","

    //add "@NotBlank " as postfix if sql schema has "NOT NULL"
    checkIfColumnCanBeNull(charListOfStringFromFile,getIndexOfSpaceAfterDataType(charListOfStringFromFile,indexAfterDataType))

    var stringWithNullableString : String
    stringWithNullableString = getNullableString(canBeNull)+string

    var primaryIdString = ""

    if (!firstLineHasBeenRead) {

        primaryIdString = "@GeneratedValue(strategy = GenerationType.IDENTITY) @Id "// with this we assume only the first has the primary key
        firstLineHasBeenRead = true
    }
    var finalString : String = """        """+primaryIdString+stringWithNullableString
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
        if (!s.toString().equals("_")) {
            if (previousCharWasAnUnderscore){
                camelCaseStringArray.add(s.toString().toUpperCase())
                previousCharWasAnUnderscore = false
            } else {
                camelCaseStringArray.add(s.toString())
            }
        } else {
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
        "tinyint" -> {
            kotlinDataType = "Int"
            kotlinDataTypeDefaultValue = "0"
        }
        "smallint" -> {
            kotlinDataType = "Int"
            kotlinDataTypeDefaultValue = "0"
        }
        "bigint" -> {
            kotlinDataType = "Long"
            kotlinDataTypeDefaultValue = "0"
        }
        "decimal" -> {
            kotlinDataType = "BigDecimal"
            kotlinDataTypeDefaultValue = "BigDecimal.ZERO"
        }
        "varchar" -> {
            kotlinDataType = "String"
            kotlinDataTypeDefaultValue = """"""""
        }
        "char" -> {
            kotlinDataType = "String"
            kotlinDataTypeDefaultValue = """"""""
        }
        "text" -> {
            kotlinDataType = "String"
            kotlinDataTypeDefaultValue = """"""""
        }
        "mediumtext" -> {
            kotlinDataType = "String"
            kotlinDataTypeDefaultValue = """"""""
        }
        "longtext" -> {
            kotlinDataType = "String"
            kotlinDataTypeDefaultValue = """"""""
        }
        "float" -> {
            kotlinDataType = "Float"
            kotlinDataTypeDefaultValue = "0.0f"
        }
        "double" -> {
            kotlinDataType = "Double"
            kotlinDataTypeDefaultValue = "0.0"
        }
        "date" -> {
            kotlinDataType = "Date"
            kotlinDataTypeDefaultValue = "Date()"
        }
        "datetime" -> {
            kotlinDataType = "Date"
            kotlinDataTypeDefaultValue = "Date()"
        }
        "timestamp" -> {
            kotlinDataType = "Date"
            kotlinDataTypeDefaultValue = "Timestamp(Date().time)"
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

    //TODO: remember that this comes immediately after the data type (whether intiated liek int(11) or not- like int.) So you have to investigate and delete
    //strings that will confuse the script, like "unsigned" which occassionally shows up right after the data type is defined
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
        "COL" -> { // TODO: Keep in mind, this was added as a hotfix for rows with "COLLATE". Most of them were nullable, so we need to look for all rows
            //with "COLLATE" which may not be nullable (don't have the "DEFAULT NULL" property) and deal with them speciaily. For example
            //all COLLATE entries which are not nullable can be not nullable on kotlin by replacing "COLLATE" with "NOT NULL" before running the script
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
        nullabeString = "@NotBlank "
    }

    return nullabeString

}

