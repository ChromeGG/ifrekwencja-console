package com.company;

import com.company.model.Subject;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static final LocalDateTime NEW_SCHOOL_YEAR_START = LocalDateTime.of(2019,9,2,6,0);
    public static final LocalDateTime TODAY = LocalDateTime.now();

    public static void main(String[] args) throws IOException, InterruptedException {

        System.setProperty("phantomjs.binary.path", "lib/PhantomJS-2.1.1-win64x/phantomjs.exe");

//        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
//        desiredCapabilities.setJavascriptEnabled(true);
//        desiredCapabilities.setCapability("takesScreenshot", false);

        PhantomJSDriver driver = new PhantomJSDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.get("https://iuczniowie.progman.pl/idziennik");
//        desiredCapabilities.setCapability("takesScreenshot", false);


        getCaptcha(driver);
//        makeSS(driver);

        logIn(driver);

        goToObecnosc(driver);

        parseSubjects(driver);

        makeSS(driver);

        System.out.println("shutdown");
        driver.quit();
    }

    private static void parseSubjects(PhantomJSDriver driver) throws InterruptedException, IOException {




//        List<WebElement> allDays = driver.findElementsByClassName("dzienMiesiaca");
//        List<WebElement> selectedDays = new ArrayList<>(Collections.emptyList());
//
//        for (WebElement element : allDays) {
//            char dayOfMonth = element.getAttribute("id").charAt(6);
//            int dayOfMonthInt = Integer.parseInt(String.valueOf(dayOfMonth));
//            if (TODAY.getDayOfMonth() < dayOfMonthInt){
//                selectedDays.add(element);
//            }
//        }
//
//        selectedDays.remove(0);




        List<WebElement> dirtyAllList = driver.findElementsByClassName("przedmiot");
        List<WebElement> clearWebElementsList = new ArrayList<>();

        for (WebElement element : dirtyAllList) {
            String text = element.getText();
            if (!(text.length() == 0 || text.contains("Ferie"))) {
                clearWebElementsList.add(element);
            }
        }

        Set<Subject> subjectSet = new HashSet<>();

        //subject initialization
        for (WebElement element : clearWebElementsList) {
            String subjectName = element.getText().substring(4);
            Subject subject = new Subject();
            subject.setName(subjectName);
            subjectSet.add(subject);
        }

        List<Subject> subjectList = new ArrayList<>(subjectSet);
        Map<String, Subject> map = subjectSet.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (WebElement element : clearWebElementsList) {
            char presenceCategory = element.getAttribute("class").charAt(element.getAttribute("class").length() - 1);

            String subjectNameFromElement = element.getText().substring(4);

            Subject subject = map.get(subjectNameFromElement);

            switch (presenceCategory) {
                case '0':
                    subject.setObecny(subject.getObecny() + 1);
                    break;
                case '1':
                    subject.setNieobecnyUsprawiedliwiony(subject.getNieobecnyUsprawiedliwiony() + 1);
                    break;
                case '2':
                    subject.setSpozniony(subject.getSpozniony() + 1);
                    break;
                case '3':
                    subject.setNieobecny(subject.getNieobecny() + 1);
                    break;
                case '4':
                    subject.setZwolnienie(subject.getZwolnienie() + 1);
                    break;
                case '5':
                    subject.setNieOdbylySie(subject.getNieOdbylySie() + 1);
                    break;
                case '9':
                    subject.setZwolnionyObecny(subject.getZwolnionyObecny() + 1);
                    break;
                default:
                        System.err.println("Cos sie zjebalo");

            }
        }

        subjectList.forEach(System.out::println);

    }

    private static void goToObecnosc(PhantomJSDriver driver) throws InterruptedException {
        Thread.sleep(500);
        WebElement btn_obecnosci = driver.findElementByXPath("//*[@id=\"btn_obecnosci\"]/a");
        btn_obecnosci.click();

        Thread.sleep(500);

        WebElement widokMiesieczny = driver.findElementByXPath("//*[@id=\"wiadomosci_main\"]/tbody/tr[1]/td/div[2]/div/div");
        widokMiesieczny.click();

        // TUTAJ TRZEBA COs WYMYSLIC ZEBY KLIKAL JAK BEDZIE KONIEC LADOWANIA
    }

    private static void logIn(PhantomJSDriver driver) {

        new WebDriverWait(driver, 20).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        Scanner scanner = new Scanner(System.in);


        WebElement nazwaSzkoly = driver.findElementById("NazwaSzkoly");
        WebElement userName = driver.findElementById("UserName");
        WebElement password = driver.findElementById("Password");
        WebElement captcha = driver.findElementById("captcha");
        WebElement btnLogin = driver.findElementByClassName("btnLogin");

        nazwaSzkoly.sendKeys("zsotlubliniec");
        userName.sendKeys("adatka_74442");
        password.sendKeys("Maslo1212");


        String captchaString = scanner.next();
        captcha.sendKeys(captchaString);

        btnLogin.click();
    }

    private static void makeSS(PhantomJSDriver driver) throws IOException {
        TakesScreenshot ts = driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(source, new File("screen.png"));
        System.out.println("Screenshot created");
    }

    private static void getCaptcha(PhantomJSDriver driver) throws IOException {

        new WebDriverWait(driver, 20).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        byte[] arrScreen = driver.getScreenshotAs(OutputType.BYTES);
        BufferedImage imageScreen = ImageIO.read(new ByteArrayInputStream(arrScreen));
        WebElement cap = driver.findElementById("imgCaptcha");

        Dimension capDimension = cap.getSize();
        Point capLocation = cap.getLocation();

        BufferedImage imgCap = imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height);

        ImageIO.write(imgCap, "png", new File("test.png"));
        System.out.println("Captcha caught");
    }
}
