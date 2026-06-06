import java.io.File;
import java.util.Scanner;

/**
 * 检测指定目录下 5 层内的：
 * 1. 空文件夹
 * 2. 空文件（大小为0）
 * 并将路径特殊标记输出
 */
public class EmptyFileChecker {

    // 最多检查 5 层目录
    private static final int MAX_DEPTH = 6;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入要检查的文件夹路径：");
        String rootPath = scanner.nextLine();

        File rootDir = new File(rootPath);

        // 判断路径是否存在
        if (!rootDir.exists()) {
            System.out.println("❌ 路径不存在！");
            return;
        }
        if (!rootDir.isDirectory()) {
            System.out.println("❌ 输入的不是文件夹！");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("开始检查【最多5层目录】中的空文件夹 & 空文件...");
        System.out.println("========================================\n");

        // 开始递归检查（从第0层开始）
        checkDir(rootDir, 0);

        System.out.println("\n✅ 检查完成！");
    }

    /**
     * 递归检查目录
     * @param dir 当前目录
     * @param currentDepth 当前层级
     */
    private static void checkDir(File dir, int currentDepth) {
        // 超过 5 层，不再继续
        if (currentDepth > MAX_DEPTH) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            // 空文件夹 → 特殊标记
            System.out.println("【⚠️ 空文件夹】" + dir.getAbsolutePath());
            return;
        }

        // 遍历当前目录所有内容
        for (File file : files) {
            if (file.isFile()) {
                // 检查是否是空文件
                if (file.length() == 0) {
                    System.out.println("【⚠️ 空文件】" + file.getAbsolutePath());
                }
            } else if (file.isDirectory()) {
                // 递归检查子目录
                checkDir(file, currentDepth + 1);
            }
        }
    }
}