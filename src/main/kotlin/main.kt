import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.github.cdimascio.dotenv.dotenv;
import java.io.File
import java.io.PrintWriter

suspend fun parseListing(out : PrintWriter, parser : Parser, listingData : JsonObject, etsy_api : String, client : HttpClient, dotenv : Dotenv) {
    for (listing in listingData.get("results") as JsonArray<JsonObject>) {
      var thisListing = ""

      thisListing += listing.get("listing_id").toString() + "\t"
      thisListing += listing.get("title").toString() + "\t"
      thisListing += listing.get("description").toString().replace("\n", " ") + "\t"
      thisListing += "in stock" + "\t"
      thisListing += "new" + "\t"
      thisListing += listing.get("price").toString() + "\t"
      thisListing += listing.get("url").toString().replace("https://www", "https://marycary") + "\t"

      var count = 0
      var first_image = ""
      val images = mutableListOf<String>()
      val imageResponse : HttpResponse = client.get("${etsy_api}listings/${listing.get("listing_id")}/images?api_key=${dotenv.get("ETSY_KEY")}")
      val imageText = StringBuilder(imageResponse.readText())
      val jsonImages: JsonObject = parser.parse(imageText) as JsonObject
      for (image in jsonImages.get("results") as JsonArray<JsonObject>) {
        if (count == 0) {
          first_image = image.get("url_fullxfull").toString()
        } else if (count < 20) {
          images.add(image.get("url_fullxfull").toString())
        }
        count++
      }

      thisListing += first_image + "\t"
      thisListing += "Mary's Beads and Accessories" + "\t"
      thisListing += images.joinToString(",")

      out.println(thisListing)
    }
}

suspend fun main() {

  val dotenv = dotenv {filename = "test.env"}
  val client = HttpClient(CIO)
  val parser: Parser = Parser.default()
  val listings = mutableListOf<String>()

  val etsy_api = "https://openapi.etsy.com/v2/"
  val listing_string = "shops/${dotenv.get("SHOP_ID")}/listings/active?api_key=${dotenv.get("ETSY_KEY")}"
  var page = "1"
  var continueLoop = true

  File("etsy_listings.tsv").printWriter().use { out ->
    out.println("id\ttitle\tdescription\tavailability\tcondition\tprice\tlink\timage_link\tbrand\tadditional_image_link")
    while(continueLoop) {
      println("Page: ${page}")
      val response: HttpResponse = client.get("${etsy_api}${listing_string}&page=${page}")
      val jsonText = StringBuilder(response.readText())
      val json: JsonObject = parser.parse(jsonText) as JsonObject
      val next_page = (json.get("pagination") as JsonObject).get("next_page")
      if(next_page == null) {
        continueLoop = false
      } else {
        page = next_page.toString()
      }
      parseListing(out, parser, json, etsy_api, client, dotenv)
    }
  }
}