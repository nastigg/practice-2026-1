/**
 * Материал объекта по модели Фонга.
 * <p>
 * Модель Фонга разбивает освещение на три составляющие:
 * ambient  = ka * ia                          (фоновый свет)
 * diffuse  = kd * id * (N · L)               (рассеянный свет)
 * specular = ks * is * (V · R)^shininess      (блик)
 * <p>
 * ka, kd, ks — коэффициенты (от 0 до 1)
 * color — базовый цвет объекта (RGB, каждый от 0 до 1)
 */
public class Material {
    public final Vec3 color;      // базовый цвет
    public final double ka;       // коэффициент фонового освещения
    public final double kd;       // коэффициент рассеянного освещения
    public final double ks;       // коэффициент зеркального блика
    public final double shininess;// "острота" блика

    public Material(Vec3 color, double ka, double kd, double ks, double shininess) {
        this.color = color;
        this.ka = ka;
        this.kd = kd;
        this.ks = ks;
        this.shininess = shininess;
    }
}
