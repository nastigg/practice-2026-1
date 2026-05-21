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
