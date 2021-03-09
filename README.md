# Etsy to Facebook

This program creates a simple tsv file of all active listings on an etsy shop. This file can then be uploaded to facebook for commerce bulk upload.


Create a `test.env` file with following information:

```
ETSY_KEY=""
SHOP_ID=""
```


Run `./gradlew run` and it will create a file called `etsy_listings.tsv` which you can then upload directly to facebook.