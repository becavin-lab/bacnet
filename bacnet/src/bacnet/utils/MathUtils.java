package bacnet.utils;

public class MathUtils {

    /**
     * Calculate the mean between two numbers
     * 
     * @param value1
     * @param value2
     * @return
     */
    public static double mean(double value1, double value2) {
        return (value1 + value2) / 2;
    }

    /**
     * Calculate log with base 2
     * 
     * @param num
     * @return
     */
    public static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }

    /**
     * Transform a value, according to the type of transformation<br>
     * type = 0 -> log2(value)<br>
     * type = 1 -> 2^value<br>
     * type = 2 -> log10(value)<br>
     * 
     * @param value
     * @param type
     * @return
     */
    public static double transform(double value, int type) {
        switch (type) {
            case 0: // log2(value)
                value = log2(value);
                break;
            case 1: // 2^value
                value = Math.pow(2, value);
                break;
            case 2: // log10(value)
                value = Math.log10(value);
                break;
        }
        return value;
    }

    /**
     * Calculate the combination C_nk
     * 
     * @param n
     * @param k
     * @return
     */
    public static int C_nk(int n, int k) {
        int result = factorial(n) / (factorial(n - k) * factorial(k));
        return result;
    }

    /**
     * Calculate the argument A_nk
     * 
     * @param n
     * @param k
     * @return
     */
    public static int A_nk(int n, int k) {
        int result = factorial(n) / (factorial(n - k));
        return result;
    }

    /**
     * Calculate the factorial of n
     * 
     * @param n
     * @return
     */
    public static int factorial(int n) {
        if (n == 0)
            return 1;
        int result = n;
        for (int i = 1; i < n; i++) {
            result *= (n - i);
        }
        return result;
    }

    /**
     * Test if a double is an integer or not
     * 
     * @param d
     * @return
     */
    public static boolean isInteger(double d) {
        if (d - Math.rint(d) == 0)
            return true;
        else
            return false;
    }
}
