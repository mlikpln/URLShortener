import org.example.URLInfo;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class URLShortener {
    private static final Map<String, URLInfo> links = new HashMap<>();
    private static final String UUID_FILE = "user_uuid.txt"; // Файл для хранения UUID
    private static final String CONFIG_FILE = "config.properties"; // Файл для конфигурации
    private static int defaultClickLimit = 100;
    private static long defaultLifetime = TimeUnit.DAYS.toSeconds(1); // 1 день по умолчанию

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        loadConfig();  // Загрузка настроек из конфигурационного файла

        String userUUID = loadUserUUID();  // Загрузка или создание UUID
        System.out.println("Ваш UUID: " + userUUID);

        while (true) {
            System.out.println("\n1. Создать короткую ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Удалить ссылку");
            System.out.println("4. Выход");
            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Очистить буфер

            switch (choice) {
                case 1:
                    createShortLink(scanner, userUUID);
                    break;
                case 2:
                    visitShortLink(scanner);
                    break;
                case 3:
                    deleteShortLink(scanner, userUUID);
                    break;
                case 4:
                    System.out.println("Выход...");
                    return;
                default:
                    System.out.println("Неверный выбор. Повторите.");
            }
        }
    }

    private static void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            defaultClickLimit = Integer.parseInt(prop.getProperty("defaultClickLimit", "100"));
            defaultLifetime = Long.parseLong(prop.getProperty("defaultLifetime", "86400")); // 1 день
        } catch (IOException ex) {
            System.out.println("Ошибка при загрузке конфигурации. Используются значения по умолчанию.");
        }
    }

    // Сохранение UUID пользователя в файл
    private static void saveUserUUID(String userUUID) {
        try (FileWriter writer = new FileWriter(UUID_FILE)) {
            writer.write(userUUID);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении UUID: " + e.getMessage());
        }
    }

    // Загрузка UUID пользователя из файла
    private static String loadUserUUID() {
        try (Scanner fileScanner = new Scanner(new File(UUID_FILE))) {
            if (fileScanner.hasNext()) {
                return fileScanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл с UUID не найден. Создаём новый.");
        }
        String userUUID = UUID.randomUUID().toString();
        saveUserUUID(userUUID);  // Сохранить новый UUID
        return userUUID;
    }

    // Создание короткой ссылки
    private static void createShortLink(Scanner scanner, String userUUID) {
        System.out.print("Введите URL для сокращения: ");
        String originalURL = scanner.nextLine();

        System.out.print("Введите лимит переходов (по умолчанию " + defaultClickLimit + "): ");
        int clickLimit = scanner.hasNextInt() ? scanner.nextInt() : defaultClickLimit;
        scanner.nextLine(); // Очистить буфер ввода

        System.out.print("Введите время жизни ссылки в секундах (по умолчанию " + defaultLifetime + "): ");
        long lifetime = scanner.hasNextLong() ? scanner.nextLong() : defaultLifetime;
        scanner.nextLine(); // Очистка буфера

        String shortLink = UUID.randomUUID().toString().substring(0, 8); // Генерация уникальной короткой ссылки
        long expiryTime = System.currentTimeMillis() + (lifetime * 1000);

        URLInfo urlInfo = new URLInfo(originalURL, clickLimit, expiryTime, userUUID);
        links.put(shortLink, urlInfo);

        System.out.println("Короткая ссылка создана: http://short.ly/" + shortLink);
    }

    // Переход по короткой ссылке
    private static void visitShortLink(Scanner scanner) {
        System.out.print("Введите короткую ссылку: ");
        String shortLink = scanner.nextLine();

        if (!links.containsKey(shortLink)) {
            System.out.println("Ссылка не найдена.");
            return;
        }

        URLInfo urlInfo = links.get(shortLink);

        // Проверка срока действия ссылки
        if (System.currentTimeMillis() > urlInfo.expiryTime) {
            System.out.println("Срок действия ссылки истёк.");
            links.remove(shortLink); // Удаление ссылки
            return;
        }

        // Проверка лимита переходов
        if (urlInfo.clicks >= urlInfo.clickLimit) {
            System.out.println("Лимит переходов по ссылке исчерпан.");
            return;
        }

        // Увеличиваем счетчик переходов
        urlInfo.clicks++;

        // Открываем браузер
        try {
            Desktop.getDesktop().browse(new URI(urlInfo.originalURL));
        } catch (IOException | URISyntaxException e) {
            System.out.println("Ошибка при открытии ссылки в браузере.");
        }

        System.out.println("Перенаправление на: " + urlInfo.originalURL);
    }

    // Удаление ссылки
    private static void deleteShortLink(Scanner scanner, String userUUID) {
        System.out.print("Введите короткую ссылку для удаления: ");
        String shortLink = scanner.nextLine();

        if (!links.containsKey(shortLink)) {
            System.out.println("Ссылка не найдена.");
            return;
        }

        URLInfo urlInfo = links.get(shortLink);

        // Проверка, является ли пользователь создателем ссылки
        if (!urlInfo.creatorUUID.equals(userUUID)) {
            System.out.println("Вы не можете удалить чужую ссылку.");
            return;
        }

        links.remove(shortLink);
        System.out.println("Ссылка удалена.");
    }
}