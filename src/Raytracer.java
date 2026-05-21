import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Raytracer — основной класс.
 *
 * Алгоритм трассировки лучей (ray tracing):
 * 1. Для каждого пикселя экрана — бросаем луч из камеры через этот пиксель.
 * 2. Находим, какой объект сцены луч пересекает первым.
 * 3. Вычисляем цвет в точке пересечения по модели освещения Фонга.
 * 4. Для теней: бросаем дополнительный луч к источнику света.
 * 5. Записываем цвет в пиксель.
 */
public class Raytracer {

    // Параметры изображения 
    static final int WIDTH  = 1024;
    static final int HEIGHT = 500;

    // Параметры камеры 
    static final Vec3 CAMERA = new Vec3(0, 0, 0);    // камера в начале координат
    static final double FOV  = Math.PI / 3.0;         // поле зрения (60°)
    // Плоскость проекции находится на расстоянии 1 от камеры (z = -1)

    // Фоновый свет сцены 
    static final Vec3 AMBIENT_INTENSITY = new Vec3(0.15, 0.15, 0.15);

    public static void main(String[] args) throws Exception {

        // Сцена: сферы 
        List<Sphere> spheres = List.of(
            // Синяя маленькая сфера (слева-сверху)
            new Sphere(
                new Vec3(-3.5, 1.5, -7),
                1.0,
                new Material(new Vec3(0.2, 0.2, 0.9), 0.1, 0.6, 0.8, 60)
            ),
            // Большая красная сфера (в центре)
            new Sphere(
                new Vec3(0.5, 0, -9),
                2.5,
                new Material(new Vec3(0.8, 0.1, 0.1), 0.1, 0.7, 0.5, 30)
            ),
            // Тёмно-зелёная сфера (справа)
            new Sphere(
                new Vec3(4.5, -0.5, -8),
                1.8,
                new Material(new Vec3(0.05, 0.35, 0.05), 0.1, 0.6, 0.4, 20)
            )
        );

        // Сцена: источники света 
        List<Light> lights = List.of(
            new Light(new Vec3(-4, 5, -3), new Vec3(0.9, 0.9, 0.9)),  // основной
            new Light(new Vec3(5, 3, -5),  new Vec3(0.5, 0.4, 0.6))   // заполняющий
        );

        // Рендеринг
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        double aspectRatio = (double) WIDTH / HEIGHT;
        // Масштаб: tan(FOV/2) даёт половину высоты плоскости проекции
        double scale = Math.tan(FOV / 2.0);

        for (int py = 0; py < HEIGHT; py++) {
            for (int px = 0; px < WIDTH; px++) {

                // Переводим пиксель в NDC (Normalized Device Coordinates), затем в мировые
                // x: от -1 до +1 с учётом соотношения сторон
                // y: от +1 до -1 (ось Y экрана перевёрнута)
                double ndcX = (2.0 * (px + 0.5) / WIDTH  - 1.0) * aspectRatio * scale;
                double ndcY = (1.0 - 2.0 * (py + 0.5) / HEIGHT) * scale;

                // Направление луча из камеры через данный пиксель
                Vec3 direction = new Vec3(ndcX, ndcY, -1).normalize();
                Ray ray = new Ray(CAMERA, direction);

                // Вычисляем цвет пикселя
                Vec3 color = trace(ray, spheres, lights);

                // Преобразуем [0,1] → [0,255] и записываем в изображение
                color = color.clamp();
                int r = (int)(color.x * 255);
                int g = (int)(color.y * 255);
                int b = (int)(color.z * 255);
                image.setRGB(px, py, (r << 16) | (g << 8) | b);
            }
        }

        //  Сохраняем PNG
        File out = new File("/mnt/user-data/outputs/raytracer_output.png");
        out.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", out);
        System.out.println("Готово! Изображение сохранено: " + out.getAbsolutePath());
    }

    /**
     * Бросаем луч в сцену и возвращаем цвет.
     * Если луч ни во что не попал — возвращаем чёрный фон.
     */
    static Vec3 trace(Ray ray, List<Sphere> spheres, List<Light> lights) {
        // 1. Ищем ближайшее пересечение
        Sphere hitSphere = null;
        double closestT = Double.MAX_VALUE;

        for (Sphere sphere : spheres) {
            double t = sphere.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                hitSphere = sphere;
            }
        }

        // Луч ничего не задел — возвращаем фон (чёрный)
        if (hitSphere == null) {
            return new Vec3(0, 0, 0);
        }

        // 2. Вычисляем точку пересечения и нормаль
        Vec3 hitPoint = ray.at(closestT);
        Vec3 normal   = hitSphere.normalAt(hitPoint);
        Material mat  = hitSphere.material;

        // 3. Считаем освещение по модели Фонга
        return phongShading(hitPoint, normal, ray, mat, spheres, lights);
    }

    /**
     * Модель освещения Фонга:
     *   color = ambient + Σ_lights (diffuse + specular) * shadow
     *
     * @param point   точка пересечения на поверхности
     * @param normal  нормаль к поверхности в этой точке
     * @param ray     исходный луч камеры
     * @param mat     материал объекта
     * @param spheres все сферы (для теней)
     * @param lights  источники света
     */
    static Vec3 phongShading(Vec3 point, Vec3 normal, Ray ray,
                             Material mat, List<Sphere> spheres, List<Light> lights) {

        // Фоновая составляющая: ka * ia * цвет объекта
        Vec3 color = mat.color.multiply(AMBIENT_INTENSITY).scale(mat.ka);

        // Вектор "к наблюдателю" (от точки к камере)
        Vec3 V = ray.direction.scale(-1).normalize();

        for (Light light : lights) {
            // Вектор от точки к источнику света
            Vec3 L = light.position.sub(point).normalize();

            // Проверка тени
            // Бросаем луч из точки к источнику. Если что-то мешает — точка в тени.
            boolean inShadow = isInShadow(point, light, spheres);
            if (inShadow) continue;

            // Диффузная составляющая (рассеянный свет)
            // kd * id * max(0, N·L)
            // N·L — косинус угла между нормалью и направлением к свету.
            // Чем прямее падает свет, тем ярче.
            double diffuseFactor = Math.max(0, normal.dot(L));
            Vec3 diffuse = mat.color
                .multiply(light.intensity)
                .scale(mat.kd * diffuseFactor);

            // Зеркальная составляющая (блик)
            // R = 2(N·L)N - L  (вектор отражения света от поверхности)
            // ks * is * max(0, V·R)^shininess
            Vec3 R = normal.scale(2 * normal.dot(L)).sub(L).normalize();
            double specularFactor = Math.pow(Math.max(0, V.dot(R)), mat.shininess);
            Vec3 specular = light.intensity.scale(mat.ks * specularFactor);

            color = color.add(diffuse).add(specular);
        }

        return color;
    }

    /**
     * Проверяет, находится ли точка в тени от данного источника света.
     * Бросаем "теневой луч" от точки к свету и проверяем — не пересекает ли
     * он какую-нибудь другую сферу на пути.
     */
    static boolean isInShadow(Vec3 point, Light light, List<Sphere> spheres) {
        Vec3 toLight = light.position.sub(point);
        double distToLight = toLight.length();
        Ray shadowRay = new Ray(point, toLight);

        for (Sphere sphere : spheres) {
            double t = sphere.intersect(shadowRay);
            // Пересечение есть и оно ближе источника — значит тень
            if (t > 0.001 && t < distToLight) {
                return true;
            }
        }
        return false;
    }
}
