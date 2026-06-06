/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName User
 * @date 2026-05-29 8:58
 */


public class User {
    // 饿汉式
//    private static final User instance = new User();
//
//    private User(){}
//
//    public static User getInstance(){
//        return instance;
//    }
    // 懒汉式
    private static User instance;

    private User(){}

    public static User getInstance(){
        if(instance == null){
            instance = new User();
        }
        return instance;
    }
}
