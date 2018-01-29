package com.example.salesforce

import java.io._
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.client.ResponseHandler
import com.typesafe.config.ConfigFactory
import com.google.gson.Gson

case class Token(
    access_token: String,
    instance_url: String,
    id: String,
    token_type: String,
    issued_at: String,
    signature: String)

class Util {

    val LOGIN_URL = "https://login.salesforce.com"
    val GRANT_SERVICE = "/services/oauth2/token?grant_type=password"

    def getAccessToken() : String = {
            val conf = ConfigFactory.load("salesforce")
            val UserName = conf.getString("force.UserName")
            val PassWord     = conf.getString("force.PassWord")
            val ClientID     = conf.getString("force.ClientID")
            val ClientSecret = conf.getString("force.ClientSecret")

            val loginURL = LOGIN_URL +
                      GRANT_SERVICE +
                      "&client_id=" + ClientID +
                      "&client_secret=" + ClientSecret +
                      "&username=" + UserName +
                      "&password=" + PassWord

            val client = new DefaultHttpClient
            val post = new HttpPost(loginURL)
            val handler = new BasicResponseHandler();
            var accessToken = ""
            try {
                val response = client.execute(post)
                val body = handler.handleResponse(response);
                val gson = new Gson
                val jsonObject = gson.fromJson(body, classOf[Token])
                accessToken = jsonObject.access_token

            } catch {
              case ioe: java.io.IOException =>
              case ste: java.net.SocketTimeoutException =>
            }

            accessToken

        }
}
