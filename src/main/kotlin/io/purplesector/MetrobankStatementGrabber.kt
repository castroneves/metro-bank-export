package io.purplesector

import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class MetrobankStatementGrabber {
    fun downloadStatements() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_URI);
        val driver = ChromeDriver()
        val webDriverWait = WebDriverWait(driver, 10L)
        driver.get("https://personal.metrobankonline.co.uk/login/")

        driver.findElementById("USER_NAME")?.sendKeys(METROBANK_CUSTOMER_NUMER)

        val buttons = driver.findElementsByClassName("mat-flat-button")
        buttons[0].click()

        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("password0")))


        driver.findElementById("password0").sendKeys(METROBANK_PASSWORD)

        val legend = driver.findElementById("”legend”")
        val indexes = legend.text
            .replace("and", ",")
            .replace(Regex("[A-Za-z ]"), "")
            .split(",")
            .map { it.toInt() - 1 }
            .map { METROBANK_PASSCODE[it] }

        (0..2).forEach { driver.findElementById("security$it").sendKeys(indexes[it].toString()) }
        val buttonsCookie = driver.findElementsByClassName("metro-cookiebar__btn")
        buttonsCookie[0].click()

        val buttons2 = driver.findElementsByClassName("mat-flat-button")
        buttons2[0].click()

        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("app-account-details")))

        driver.findElementsByClassName("mat-card-content-inner")[0].click()

        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("app-transactions-details")))
        Thread.sleep(2000)
        clickDownload(driver)

        // wait for download
        Thread.sleep(3000)

        val menuButtons =
            driver.findElementByClassName("interval-selector-container-row").findElements(By.tagName("div"))

        (1 until MONTHS_BACK)
            .map { 5 - it }
            .forEach {
                println("Clicking button index $it")
                menuButtons[it].click()
                Thread.sleep(1500)
                clickDownload(driver)
                // wait for download
                Thread.sleep(3000)
            }

    }

    private fun clickDownload(driver: ChromeDriver) {
        driver.findElementsByClassName("download-selector-button")[0].click()
        driver.findElementsByClassName("download-item-content")[0].click()
        println()
//        driver.findElementsByClassName("download-selector-button").filter { it.text.contains("Export") }[0].click()
    }
}