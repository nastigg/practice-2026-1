import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

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
        double scale = Math.tan(FOV / 2.0);

        for (int py = 0; py < HEIGHT; py++) {
            for (int px = 0; px < WIDTH; px++) {

                // x: от -1 до +1 с учётом соотношения сторон
                // y: от +1 до -1 (ось Y экрана перевёрнута)
                double ndcX = (2.0 * (px + 0.5) / WIDTH  - 1.0) * aspectRatio * scale;
                double ndcY = (1.0 - 2.0 * (py + 0.5) / HEIGHT) * scale;

                Vec3 direction = new Vec3(ndcX, ndcY, -1).normalize();
                Ray ray = new Ray(CAMERA, direction);
                
                Vec3 color = trace(ray, spheres, lights);
                
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

    static Vec3 trace(Ray ray, List<Sphere> spheres, List<Light> lights) {
        
        Sphere hitSphere = null;
        double closestT = Double.MAX_VALUE;

        for (Sphere sphere : spheres) {
            double t = sphere.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                hitSphere = sphere;
            }
        }

        if (hitSphere == null) {
            return new Vec3(0, 0, 0);
        }

  
        Vec3 hitPoint = ray.at(closestT);
        Vec3 normal   = hitSphere.normalAt(hitPoint);
        Material mat  = hitSphere.material;

   
        return phongShading(hitPoint, normal, ray, mat, spheres, lights);
    }

    
    static Vec3 phongShading(Vec3 point, Vec3 normal, Ray ray,
                             Material mat, List<Sphere> spheres, List<Light> lights) {
       
        Vec3 color = mat.color.multiply(AMBIENT_INTENSITY).scale(mat.ka);
        Vec3 V = ray.direction.scale(-1).normalize();
        for (Light light : lights) {
            Vec3 L = light.position.sub(point).normalize();
            boolean inShadow = isInShadow(point, light, spheres);
            if (inShadow) continue;
            double diffuseFactor = Math.max(0, normal.dot(L));
            Vec3 diffuse = mat.color
                .multiply(light.intensity)
                .scale(mat.kd * diffuseFactor);
            Vec3 R = normal.scale(2 * normal.dot(L)).sub(L).normalize();
            double specularFactor = Math.pow(Math.max(0, V.dot(R)), mat.shininess);
            Vec3 specular = light.intensity.scale(mat.ks * specularFactor);
            color = color.add(diffuse).add(specular);
        }

        return color;
    }

    static boolean isInShadow(Vec3 point, Light light, List<Sphere> spheres) {
        Vec3 toLight = light.position.sub(point);
        double distToLight = toLight.length();
        Ray shadowRay = new Ray(point, toLight);

        for (Sphere sphere : spheres) {
            double t = sphere.intersect(shadowRay);
            if (t > 0.001 && t < distToLight) {
                return true;
            }
        }
        return false;
    }
}
