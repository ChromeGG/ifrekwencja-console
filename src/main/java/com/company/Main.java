package com.company;

import com.company.model.Subject;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    public static final LocalDateTime NEW_SCHOOL_YEAR_START = LocalDateTime.of(2019, 9, 2, 6, 0);
    private static final LocalDateTime TODAY = LocalDateTime.now();

    public static void main(String[] args) throws IOException, InterruptedException {

        System.setProperty("phantomjs.binary.path", "lib/PhantomJS-2.1.1-win64x/phantomjs.exe");

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
        String pageSource = driver.getPageSource();
        Document htmlDocument = Jsoup.parse(pageSource);

        Elements allDays = htmlDocument.getElementsByClass("dzienMiesiaca");
        Elements selectedDays = new Elements();


        for (Element element : allDays) {
            Element dzienMiesiacaHead = element.getElementsByClass("dzienMiesiacaHead").first();
            char dayOfMonth = dzienMiesiacaHead.text().charAt(0);
            int dayOfMonthInt = Integer.parseInt(String.valueOf(dayOfMonth));
            if (dayOfMonthInt < TODAY.getDayOfMonth()) {
                selectedDays.add(element);
            }
        }

        //remove bugged day (2 september)
        selectedDays.remove(0);

        Set<Subject> subjectSet = new HashSet<>();

        for (Element element : selectedDays) {
            Elements children = element.children();
            Iterator<Element> iterator = children.iterator();
            iterator.next(); //shift date header (example: 3 September)
            while (iterator.hasNext()) {
                String subjectName = iterator.next().text().substring(4);
                Subject subject = new Subject();
                subject.setName(subjectName);
                subjectSet.add(subject);
            }
        }

        List<Subject> subjectList = new ArrayList<>(subjectSet);
        Map<String, Subject> map = subjectSet.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (Element subjectElement : selectedDays) {
            Elements children = subjectElement.children();
            Iterator<Element> iterator = children.iterator();
            iterator.next();
            while (iterator.hasNext()){
                Element element = iterator.next();
                String classString = element.attr("class");
                char presenceCategory = classString.charAt(classString.length() - 1);

                String subjectName = element.text().substring(4);

                Subject subject = map.get(subjectName);


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
        }

        subjectSet.forEach(System.out::println);

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