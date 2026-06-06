import java.io.File;
import java.util.Scanner;

/**
 * 遍历文件夹 最多5层目录
 * 输出每一层的 目录名称 + 文件名称
 */
public class FolderLevelScanner {
    public static void main(String[] args) {
        // 1. 获取控制台输入的文件夹路径
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入项目文件夹路径：");
        String inputPath = scanner.nextLine();
        scanner.close();

        // 2. 封装文件对象并校验
        File rootDir = new File(inputPath);
        if (!rootDir.exists()) {
            System.out.println("❌ 错误：路径不存在！");
            return;
        }
        if (!rootDir.isDirectory()) {
            System.out.println("❌ 错误：输入的不是文件夹！");
            return;
        }

        System.out.println("=====================================");
        System.out.println("开始扫描【最多5层】目录结构");
        System.out.println("根目录：" + rootDir.getAbsolutePath());
        System.out.println("=====================================\n");

        // 3. 开始扫描，初始层级为 第1层
        scanFolderByLevel(rootDir, 1);

        System.out.println("\n=====================================");
        System.out.println("扫描完成（已限制最多5层）");
    }

    /**
     * 递归扫描文件夹（严格限制5层）
     * @param currentDir 当前扫描的目录
     * @param currentLevel 当前层级（1~5）
     */
    private static void scanFolderByLevel(File currentDir, int currentLevel) {
        // 核心限制：超过5层直接停止，不再往下遍历
        if (currentLevel > 5) {
            return;
        }

        // 获取当前目录下的所有文件/子目录
        File[] files = currentDir.listFiles();
        // 空文件夹 / 无访问权限 直接跳过
        if (files == null || files.length == 0) {
            return;
        }

        // 输出当前层级标题
        System.out.println("📂 第 " + currentLevel + " 层 | 当前目录：" + currentDir.getName());
        System.out.println("--------------------------------------------------");

        // 遍历当前层的所有内容
        for (File file : files) {
            if (file.isDirectory()) {
                // 输出 目录名称
                System.out.println("├─ 目录：" + file.getName());
            } else {
                // 输出 文件名称
                System.out.println("├─ 文件：" + file.getName());
            }
        }
        System.out.println(); // 空行分隔层级

        // 继续递归：遍历子目录，层级+1
        for (File file : files) {
            if (file.isDirectory()) {
                scanFolderByLevel(file, currentLevel + 1);
            }
        }
    }
}