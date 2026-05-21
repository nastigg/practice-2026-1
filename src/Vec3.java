/**
 * Трёхмерный вектор.
 * Используется и для координат точек, и для направлений, и для цветов (R, G, B).
 */
public class Vec3 {
    public final double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    /** Сложение двух векторов */
    public Vec3 add(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    /** Вычитание вектора */
    public Vec3 sub(Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    /** Умножение на скаляр */
    public Vec3 scale(double t) {
        return new Vec3(x * t, y * t, z * t);
    }

    /** Покомпонентное умножение (для смешивания цветов) */
    public Vec3 multiply(Vec3 other) {
        return new Vec3(x * other.x, y * other.y, z * other.z);
    }

    /** Скалярное (dot) произведение: |A||B|cos(угол) */
    public double dot(Vec3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /** Длина вектора */
    public double length() {
        return Math.sqrt(dot(this));
    }

    /** Нормализованный вектор (единичная длина) */
    public Vec3 normalize() {
        double len = length();
        return scale(1.0 / len);
    }

    /** Ограничить каждую компоненту в диапазоне [0, 1] (для цветов) */
    public Vec3 clamp() {
        return new Vec3(
            Math.max(0, Math.min(1, x)),
            Math.max(0, Math.min(1, y)),
            Math.max(0, Math.min(1, z))
        );
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
