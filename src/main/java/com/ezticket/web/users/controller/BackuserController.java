package com.ezticket.web.users.controller;

import com.ezticket.core.service.EmailServiceImpl;
import com.ezticket.core.service.RedisService;
import com.ezticket.core.service.VerificationCodeService;
import com.ezticket.web.users.dto.BackuserDTO;
import com.ezticket.web.users.dto.BackuserImgDTO;
import com.ezticket.web.users.dto.RoleDTO;
import com.ezticket.web.users.pojo.Backuser;
import com.ezticket.web.users.pojo.Member;
import com.ezticket.web.users.repository.BackuserRepository;
import com.ezticket.web.users.service.BackuserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backuser")
public class BackuserController {
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,12}$";

    @Autowired
    private BackuserService backuserService;
    @Autowired
    private BackuserRepository backuserRepository;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @Autowired
    private RedisService redisService;

    //拿到所有後台使用者資料
    @GetMapping("/ga")
    public List<BackuserDTO> getAllBackuser(){ return backuserService.getAllBackuser(); }

    //更新編輯後台使用者資料
    @PostMapping("/{backuserno}")
    public ResponseEntity<?> updateBackuser(@PathVariable("backuserno")Integer backuserno, @Valid @RequestBody BackuserDTO newBackuserDTO, BindingResult bindingResult){
        //測試前端輸入的資料有無進來
        System.out.println("要求編輯的後台使用者資料Backuserno: " + backuserno);
        System.out.println(newBackuserDTO.toString());
        if (bindingResult.hasErrors()) {
            System.out.println("資料格式異常");
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        } else {
            System.out.println("資料儲存完成");
            Backuser updateBackuser = backuserService.updateBackuser(backuserno,newBackuserDTO);
            return ResponseEntity.ok(updateBackuser);
        }
    }

    //後台使用者管理權限按鈕
    @PostMapping("/{bano}/auth")
    public ResponseEntity<Backuser> toggleBackuserStatus(@PathVariable("bano") Integer bano){
        Backuser updateBackuserStatus = backuserService.updateBackuserStatus(bano);
        System.out.println("Update Status Bano: " + bano);
        return ResponseEntity.ok(updateBackuserStatus);
    }


    //新增後台使用者
    @PostMapping("/insertBu")
    public ResponseEntity<?> createBackuser(@Valid @RequestBody Backuser newbackuser, BindingResult bindingResult){
        System.out.println("前台要求新增後台使用者資料:" + newbackuser.toString());
        Backuser backuser = backuserRepository.findByBaemail(newbackuser.getBaemail());
        boolean passwordFormatError = newbackuser.getBapassword() == null || !newbackuser.getBapassword().matches(PASSWORD_REGEX);
        if (bindingResult.hasErrors() || backuser != null || passwordFormatError) {
            System.out.println("新增的資料格式有誤需調整");
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            if(backuser != null){
                errors.put("baemail","此Email已註冊!");
                System.out.println("此Email已註冊過!");
            }
            if(passwordFormatError){
                errors.put("bapassword","密碼需由英文及數字組成，並且至少包含一個大寫字母、一個小寫字母，長度為8~12個字元。");
            }
            return ResponseEntity.badRequest().body(errors);
        } else {
            System.out.println("新增成功");
            newbackuser = backuserService.createBackuser(newbackuser);
            return  ResponseEntity.ok(newbackuser);
        }
    }

    //後台成員登入時的驗證且存的到Session
    @PostMapping("/login")
    public Backuser login(HttpServletRequest request,@RequestBody Backuser backuser){
        HttpSession session = request.getSession();
        if (backuserService.authenticate(backuser.getBaaccount(), backuser.getBapassword())) {
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            session.setAttribute("BuLoggedin", true);
            session.setAttribute("backuser", backuser);
            backuser.setSuccessful(true);
            System.out.println("登入驗證成功");
        } else {
            Backuser newbackuser = backuserRepository.findByBaaccount(backuser.getBaaccount());
            if(newbackuser.getBastatus() == 0){
                backuser.setBastatus(0);
                backuser.setMessage("此帳號無權限登入!");
                System.out.println("此帳號無權限登入!");
            }else {
                backuser.setMessage("查無此帳號或密碼");
                System.out.println("查無此帳號或密碼");
            }
            backuser.setSuccessful(false);
        }
        return backuser;
    }

    //後台使用者登入後,其他後台頁面可取得後台成員的基本資料
    @GetMapping("/getBackuserInfo")
    @ResponseBody
    public Backuser getBackuserInfo(HttpSession session, HttpServletResponse response) throws IOException {
       Backuser backuser = (Backuser) session.getAttribute("backuser");
        if (backuser != null) {
            backuser = backuserService.getBackuserInfo(backuser.getBaaccount());
            backuser.setSuccessful(true);
            backuser.setBapassword("第二組everybody年薪百萬!");
        } else {
            Backuser nobackuser = new Backuser();
            nobackuser.setSuccessful(false);
            nobackuser.setMessage("會員未通過登入認證");
            response.sendRedirect("/front-users-bu-sign-in.html");
            return nobackuser;
        }
        return backuser;
    }

    //後台使用者登入後,其他後台頁面可取得後台成員的權限功能資料
    @GetMapping("/getBackuserAuthInfo")
    @ResponseBody
    public  RoleDTO getBackuserAuthInfo(HttpSession session, HttpServletResponse response) throws IOException {
        Backuser backuser = (Backuser) session.getAttribute("backuser");
        RoleDTO roleDTO = new RoleDTO();
        if (backuser != null) {
            roleDTO = backuserService.getBaRolesWithFuncs(backuser.getBaaccount());
            System.out.println("取得後台成員功能成功!");
        } else {
            roleDTO = null;
            System.out.println("會員未通過認證");
            response.sendRedirect("/front-users-bu-sign-in.html");
        }
        return roleDTO;
    }




    //後台成員上傳更新自己的大頭貼
    @PostMapping("/upload-baimg")
    public ResponseEntity<String> uploadBaimg(@ModelAttribute BackuserImgDTO backuserImgDTO, @RequestParam("file") MultipartFile file, @RequestParam("baaccount") String baaccount){
        try {
            System.out.println("後台成員大頭貼上傳成功");
            backuserImgDTO.setFile(file);
            backuserImgDTO.setBaaccount(baaccount);
            backuserService.updateBaImg(backuserImgDTO);
            return ResponseEntity.ok("上傳成功");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    //後台成員於後端改密碼
    @PostMapping("/changePwd")
    @ResponseBody
    public Map<String, String> changePassword(@RequestParam("oldpassword") String oldPassword,
                                              @RequestParam("newpassword") String newPassword,
                                              @RequestParam("confirmpassword") String confirmPassword,
                                              HttpSession session) {
        System.out.println("收到改密碼請求:"+ "(舊)"+ oldPassword +" (新)"+newPassword);
        Map<String, String> resultMap = new HashMap<>();
        Backuser backuser = (Backuser) session.getAttribute("backuser");
        if (backuser == null) {
            System.out.println("會員未登入!");
            resultMap.put("noSessionError","會員未登入!");
            return resultMap;
        }
        //進Service驗證
        resultMap = backuserService.updateBapassword(backuser.getBaaccount(),oldPassword,newPassword,confirmPassword);
        return resultMap;
    }


    //後台成員自己編輯資料更新儲存
    @PostMapping("/saveBackuserEdit")
    @ResponseBody
    public ResponseEntity<?> updateBackuser(@Valid @RequestBody BackuserDTO backuserDTO, BindingResult bindingResult, HttpSession session) {
        Map<String, String> errors = new HashMap<>();
        Backuser backuser = (Backuser) session.getAttribute("backuser");
        if (backuser == null) {
            errors.put("noSessionError","後台成員未登入!");
            System.out.println("Session找不到登入的後台成員資料");
            return ResponseEntity.badRequest().body(errors);
        }
        if (bindingResult.hasErrors()) {
            System.out.println("資料格式異常");
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        } else {
            // 儲存資料到資料庫
            System.out.println("資料儲存完成,後台成員資料更新!");
            Backuser updatebackuser = backuserService.updateBackuser(backuserDTO);
            return ResponseEntity.ok(updatebackuser);
        }
    }

    //登出按鈕
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        System.out.println("登出成功");
        return ResponseEntity.ok("Logout success");
    }

    //看有沒有拿到showAlert,有的話就代表這個人沒權限所以要再backindex顯示警告
    @GetMapping("/noAuth")
    public  boolean  auth (HttpSession session) {
        session.getAttribute("showAlert");
        if(session.getAttribute("showAlert") == null){
            return false;
        }
        session.removeAttribute("showAlert");
        return true;
    }

    //忘記密碼頁面  第一步:確認有無此會員,有的話就寄驗證碼到信箱
    @RequestMapping("/checkemail/{email}")
    public boolean checkEmail(@PathVariable String email) throws MessagingException {
        Backuser backuser = backuserRepository.findByBaemail(email);
        if (backuser == null) {
            System.out.println("查無此會員!");
            return false;
        }

        System.out.println("後台驗證碼已送出!");
        String code = verificationCodeService.generateCode(email);
        emailServiceImpl.sendVerificationCode(email, code);
        return true;


    }

    //確認驗證碼是否正確
    @RequestMapping("/verify")
    public boolean verificationCode(@RequestParam String email, @RequestParam String code) {
        System.out.println(code);
        System.out.println(email);
        if (redisService.checkCode(email, code)) {
            System.out.println("驗證碼正確!");
            return true;
        }
        System.out.println("驗證碼錯誤!");
        return false;
    }

    //前台已驗證過直接儲存進去新密碼(忘記密碼)
    @PostMapping("/resetPwd")
    public void savePassword(@RequestParam("email") String email, @RequestParam("password") String password) {
        System.out.println("資料已進來");
        System.out.println(email);
        System.out.println(password);
        Backuser backuser =backuserService.updateBuPwd(email,password);
    }


}
