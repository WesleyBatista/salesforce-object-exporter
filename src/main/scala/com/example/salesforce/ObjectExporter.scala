package com.example.salesforce

/** 
 * The object to acommodate the main method that will instantiate the `SObject`
 */
object ObjectExporter {

    /** 
     * Get the arguments from the command line, pass to the `SObject` constructor and
     * call the `dumpNewlineDelimitedJson` method.
     *
     * @param args the command line arguments
     */
    def main(args: Array[String]): Unit = {
        val objectName = args(1)
        val outputPath = args(2)
        val sObject = new SObject(objectName, outputPath)
        sObject.dumpNewlineDelimitedJson
    }
}
