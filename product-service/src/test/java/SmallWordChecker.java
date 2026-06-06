import java.io.File;
import java.util.Scanner;

/**
 * 检查文件夹（含嵌套目录）中小于2KB的Word文档
 * 输出：文件名 + 完整路径
 */
public class SmallWordChecker {
    public static void main(String[] args) {
        // 1. 获取控制台输入的文件夹路径
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入需要检查的文件夹路径：");
        String inputPath = scanner.nextLine();
        scanner.close();

        // 2. 封装为文件对象
        File rootDir = new File(inputPath);

        // 3. 基础校验：路径是否存在、是否为文件夹
        if (!rootDir.exists()) {
            System.out.println("❌ 错误：输入的路径不存在！");
            return;
        }
        if (!rootDir.isDirectory()) {
            System.out.println("❌ 错误：输入的不是文件夹路径！");
            return;
        }

        System.out.println("=====================================");
        System.out.println("开始扫描：" + inputPath);
        System.out.println("扫描规则：Word文档(.doc/.docx) < 2KB");
        System.out.println("=====================================\n");

        // 4. 递归扫描所有文件
        scanFolder(rootDir);

        System.out.println("\n=====================================");
        System.out.println("扫描完成！");
    }

    /**
     * 递归遍历文件夹：目录则递归，文件则检查
     */
    private static void scanFolder(File folder) {
        File[] files = folder.listFiles();
        // 空文件夹 / 无权限 → 跳过
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            // 如果是子文件夹 → 递归扫描
            if (file.isDirectory()) {
                scanFolder(file);
            } else {
                // 判断：是Word文件 + 大小小于2KB (2*1024字节)
                if (isWordDocument(file) && file.length() < 2048) {
                    System.out.println("⚠️  发现小文件：");
                    System.out.println("文件名：" + file.getName());
                    System.out.println("路径：" + file.getAbsolutePath());
                    System.out.println("---------------------------------");
                }
            }
        }
    }

    /**
     * 判断是否为Word文档（支持.doc/.docx，忽略大小写）
     */
    private static boolean isWordDocument(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".doc") || name.endsWith(".docx");
    }
}