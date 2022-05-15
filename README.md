# Metro Bank statement importer for FreeAgent
Metro Bank sadly seem reluctant to develop an OpenBanking API, so here is a scraper you can run at will to import your statements into FreeAgent
### Usage
Ensure the configuration in Main.kt is correct, and run :) Must have Chrome and Chromedriver installed(https://chromedriver.chromium.org/downloads)

    const val MONTHS_BACK: Int = 3 -- Number of months back you wish to go (Max 6 min 1)
    const val CHROMEDRIVER_URI = "/home/user/chromedriver"
    const val DOWNLOADS_DIR: String = "/home/user/downloads" -- Where Chrome downloads files by default
    const val METROBANK_CUSTOMER_NUMER = ""
    const val METROBANK_PASSWORD: String = ""
    const val METROBANK_PASSCODE: String = ""
    const val FREEAGENT_BANK_ACCOUNT_ID: String = "" -- this can be found by GET https://api.freeagent.com/v2/bank_accounts
    const val FREEAGENT_API_KEY = ""
    const val FREEAGENT_API_SECRET = ""
    
