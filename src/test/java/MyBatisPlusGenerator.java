// 生成器通常放在test目录下或db包中，不参与生产运行

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

public class MyBatisPlusGenerator {

    public static void main(String[] args) {
        // 1. 全局配置
        GlobalConfig config = new GlobalConfig();
        config.setActiveRecord(true)               // 是否支持AR模式（本处后面关闭）
                .setAuthor("zyd")                   // 作者署名
                .setOutputDir("C:\\Users\\zhangyadong\\Desktop\\") // 输出路径
                .setFileOverride(true)              // 文件已存在时是否覆盖
                .setIdType(IdType.AUTO)             // 主键策略：数据库自增
                .setDateType(DateType.ONLY_DATE)    // 日期类型处理
                .setServiceName("%sService")        // Service接口名，首字母大写
                .setEntityName("%sDO")              // 实体类名后缀，符合DO规范
                .setBaseResultMap(true)             // 生成基本resultMap
                .setActiveRecord(false)             // 不使用AR模式（更关注POJO纯粹性）
                .setBaseColumnList(true);           // 生成基础列SQL片段

        // 2. 数据源配置
        DataSourceConfig dsConfig = new DataSourceConfig();
        dsConfig.setDbType(DbType.MYSQL)
                .setDriverName("com.mysql.cj.jdbc.Driver")
                .setUrl("jdbc:mysql://192.168.100.128:3306/shop-product?useSSL=false")
                .setUsername("root")
                .setPassword("1234");

        // 3. 策略配置
        StrategyConfig stConfig = new StrategyConfig();
        stConfig.setCapitalMode(true)                      // 全局大写命名
                .setNaming(NamingStrategy.underline_to_camel)  // 字段名下滑线转驼峰
                .setEntityLombokModel(true)                 // 实体类使用Lombok
                .setRestControllerStyle(true)               // Controller生成@RestController
                .setInclude("product_task");              // 需要生成的表名，支持多表

        // 4. 包名策略配置
        PackageConfig pkConfig = new PackageConfig();
        pkConfig.setParent("com.hxy")          // 父包路径
                .setMapper("mapper")            // Mapper接口所在子包
                .setService("service")          // Service接口所在子包
                .setController("controller")    // Controller所在子包
                .setEntity("model")             // 实体类所在子包（命名为model而非entity）
                .setXml("mapper");              // XML文件所在资源子目录

        // 5. 整合配置并执行
        AutoGenerator ag = new AutoGenerator();
        ag.setGlobalConfig(config)
          .setDataSource(dsConfig)
          .setStrategy(stConfig)
          .setPackageInfo(pkConfig);

        ag.execute();
        System.out.println("=======  Done 相关代码生成完毕  ========");
    }
}