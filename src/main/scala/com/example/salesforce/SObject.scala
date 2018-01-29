package com.example.salesforce

import java.util.List
import java.io.{BufferedWriter, File, FileWriter}
import org.apache.http.impl.client.{DefaultHttpClient,BasicResponseHandler}
import org.apache.http.client.methods.HttpGet
import com.typesafe.config.ConfigFactory
import com.google.gson.{Gson, JsonParser, JsonArray}
import scala.collection.JavaConversions._


/** 
 * A Salesforce Object records exporter
 *
 * @param sObjectN literal name of the Object at Salesforce (capital sensitive)
 * @param outputPath absolute and writable output path for the records writing
 */
class SObject(sObjectN: String, outputPath: String) {
    val conf = ConfigFactory.load("salesforce")
    val HOST = conf.getString("force.InstanceUrl")
    val DATA_SERVICE_URL = conf.getString("force.DataServiceUrl")

    var sObjectName: String = sObjectN
    val util = new Util()
    val gson = new Gson
    var parser = new JsonParser
    private val accessToken = util.getAccessToken()

    /**
     * Using the `describe` response we build a Array that contains the Object fields
     */
    var fields = {
        var result: Array[String] = Array("")
        val response = describe
        val describeResponse = gson.fromJson(response, classOf[DescribeResponse])
        result = describeResponse.fields.map(x => {
            x.name
        })
        result
    }

    var query = {
        fields.mkString("SELECT+", ",+", "+FROM+"+f"$sObjectName%s")
    }

    /**
     * Execute a GET request, adding the needed headers for authorization and response 
     * handling.
     * 
     * @param url Salesforce API's URL
     * @return String representing the response from the GET request
     */
    def requestGet(url:String): String = {
        val request = new HttpGet(url)
        request.addHeader("Authorization", "Bearer " + accessToken)
        request.addHeader("Content-type", "application/json")
        val client = new DefaultHttpClient
        val response = client.execute(request)
        val handler = new BasicResponseHandler()
        val body = handler.handleResponse(response)
        body
    }

    /**
     * Retrieves the parsed results from `queryAll` API endpoint
     * 
     * 
     * @param identifier The identifier used in an additional request to retrieve the next batch.
     * @return JsonObject from the Gson library
     */
    def getPaginated(identifier: String) = {
        val url = HOST + identifier
        val response = requestGet(url)
        responseParser(response)
    }

    /**
     * Parses the response to a known default schema 
     * 
     * @param response String to be serialized to Scala
     * @return JsonObject from the Gson library
     */
    def responseParser(response: String) = {
        val jsonObject = gson.fromJson(response, classOf[Response])
        parser.parse(gson.toJson(jsonObject)).getAsJsonObject
    }

    /**
     * Hits the Salesforce endpoint that describes the 'Object', so we can know the columns to 
     * create the query.
     * 
     * @return String
     */
    def describe = {
        val baseUrl = f"/sobjects/$sObjectName%s/describe"
        val url = HOST + DATA_SERVICE_URL + baseUrl
        val body = requestGet(url)
        body
    }

    /**
     * Hit the API to get the results. Parse all the 'records' retrieved.
     * Do it over and over while the `response.done` field is not set to `true`.
     * 
     * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_query.htm
     * 
     * @return JsonArray from the Gson library
     */
    def retrieveRecords = {
        var parsedRecords = new JsonArray
        var identifier = query
        val queryResponse = executeSOQL(query)
        var response = responseParser(queryResponse)
        var allRecords = parser.parse(gson.toJson(response.get("records"))).getAsJsonArray
        var done = response.get("done").getAsBoolean
        while(done == false) {
            identifier = response.get("nextRecordsUrl").getAsString
            println(identifier)
            println(done.toString)
            response = getPaginated(identifier)
            parsedRecords = parser.parse(gson.toJson(response.get("records"))).getAsJsonArray
            allRecords.addAll(parsedRecords)
            done = response.get("done").getAsBoolean
        }
        allRecords
    }

    /**
     * Mount the URL using the `DATA_SERVICE_URL` config and the `soql` argument, make the
     * call and retrieve the results.
     * 
     * @param soql String that can be a Salesforce Object Query Language string itself or the query 
     * identifier (the later one used to retrieve the next bacth of results).
     * @return a response from `queryAll` endpoint
     */
    def executeSOQL(soql: String): String = {
        val baseUrl = DATA_SERVICE_URL + "/queryAll/?q="
        val url = HOST + baseUrl  + soql
        val body = requestGet(url)
        body
    }

    /**
     * Iterate over the `retrieveRecords` to get each object as String and write the a file buffer
     * It will write the results the `outputPath` set on `SObject` instantiation.
     */
    def dumpNewlineDelimitedJson = {
        val file = new File(outputPath)
        val bw = new BufferedWriter(new FileWriter(file))
        retrieveRecords.foreach(x => {
            bw.write(x.toString)
            bw.newLine()
        })
        bw.close()
    }
}
