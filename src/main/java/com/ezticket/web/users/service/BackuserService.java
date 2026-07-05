package com.ezticket.web.users.service;

import com.ezticket.web.users.dto.BackuserDTO;
import com.ezticket.web.users.dto.BackuserImgDTO;
import com.ezticket.web.users.dto.RoleDTO;
import com.ezticket.web.users.pojo.*;
import com.ezticket.web.users.repository.BackuserRepository;
import com.ezticket.web.users.repository.FunctionRepository;
import com.ezticket.web.users.repository.RoleRepository;
import com.ezticket.web.users.repository.RoleauthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BackuserService {

    @Autowired
    private BackuserRepository backuserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleauthorityRepository roleauthorityRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;

    //拿取所有後台成員的資料,過濾只要DTO的內容
    public List<BackuserDTO> getAllBackuser() {
        return backuserRepository.findAll()
                .stream()
                .map(backuser -> BackuserDTO.builder()
                        .bano(backuser.getBano())
                        .baaccount(backuser.getBaaccount())
                        .baname(backuser.getBaname())
                        .baemail(backuser.getBaemail())
                        .baroleno(backuser.getBaroleno())
                        .bacell(backuser.getBacell())
                        .bastatus(backuser.getBastatus())
                        .build())
                .collect(Collectors.toList());
    }

    //更新後台成員資料,只針對DTO的欄位即可
    public Backuser updateBackuser(Integer backuserno,BackuserDTO newBackuserDTO){
        Optional<Backuser> oldBackuser = backuserRepository.findById(backuserno);
        if(oldBackuser.isPresent()){
            Backuser updateTheBackuser = oldBackuser.get();
            updateTheBackuser.setBaname(newBackuserDTO.getBaname());
            updateTheBackuser.setBaemail(newBackuserDTO.getBaemail());
            updateTheBackuser.setBaroleno(newBackuserDTO.getBaroleno());
            updateTheBackuser.setBacell(newBackuserDTO.getBacell());
            return  backuserRepository.save(updateTheBackuser);
        }else {
            throw new RuntimeException("Backuser not found with backuserno: " + backuserno);
        }
    }

    //toggle後台成員的權限狀態  1 <-> 0
    public Backuser updateBackuserStatus(Integer bano){
        Optional<Backuser> oldBackuser = backuserRepository.findById(bano);
        if(oldBackuser.isPresent()){
            Backuser updateTheBa = oldBackuser.get();
            if (updateTheBa.getBastatus() == 1) {
                updateTheBa.setBastatus(0);
            } else if (updateTheBa.getBastatus() == 0) {
                updateTheBa.setBastatus(1);
            }
            return backuserRepository.save(updateTheBa);

        }else {
            throw new RuntimeException("Backuser not found with bano: " + bano);
        }
    }

    //驗證資料庫有無此後台成員,回傳布林值
    public boolean authenticate(String baaccount, String bapassword) {
        Backuser backuser = backuserRepository.findByBaaccount(baaccount);
        if (backuser == null) {
            // 帳號不存在也跑一次雜湊比對，避免以回應時間差列舉有效帳號
            bcryptPasswordEncoder.matches(bapassword != null ? bapassword : "", TIMING_DUMMY_HASH);
            return false;
        }
        return passwordMatchesAndUpgrade(backuser, bapassword) && backuser.getBastatus() == 1;
    }

    //取得單一後台成員的所有基本資料 //未寫完
    public Backuser getBackuserInfo(String baaccount){
        Backuser backuser = backuserRepository.findByBaaccount(baaccount);
        return backuser;
    }

    //透過後台成員的baroleno,來取得單一後台成員的角色/擁有功能/沒有的功能
    public RoleDTO getBaRolesWithFuncs(String baaccount){
        RoleDTO roleDTO = new RoleDTO();
        Backuser backuser = backuserRepository.findByBaaccount(baaccount);
        Optional<Role> role = roleRepository.findById(backuser.getBaroleno());
        roleDTO.setRoleno(backuser.getBaroleno());
        roleDTO.setRolename(role.get().getRolename());
        roleDTO.setRolestatus(role.get().getRolestatus());
        //根據後台使用者的角色,存取此角色擁有的功能進DTO
        Map<Integer,String> funcMap = new HashMap<>();
        //把角色的所有功能{funcno:funcname}放進List
        List<Roleauthority> roleauthorities = roleauthorityRepository.findByRoleno(role.get().getRoleno());
        //透過角色的所有功能的funcno來找到funcno和funcname並放入funcMap
        for (Roleauthority roleauthority : roleauthorities){
            if (roleauthority.getFuncno() != 1 && roleauthority.getFuncno() != 2) {
                //直接省略顯示所有角色的funcno1跟2,因為是基本功能一定要有的,不需要被增加或減少
                Function allfuncs = functionRepository.findByFuncno(roleauthority.getFuncno());
                funcMap.put(allfuncs.getFuncno(),allfuncs.getFuncname());
            }
        }
        //存入roleDTO
        roleDTO.setFuncs(funcMap);


        //存取此角色缺少的功能進DTO
        //取該角色"擁有的功能"的所有key(funcno)放進Set
        Set<Integer> roleFuncKey = funcMap.keySet();

        //取得功能表格裡的"所有功能"的funcno放進第二個Set
        Set<Integer> allFuncKey = new HashSet<>();
        List<Function> allFuncs = functionRepository.findAll();
        for(Function funcs : allFuncs){
            //直接省略顯示所有角色的funcno1跟2,因為是基本功能一定要有的,不需要被增加或減少
            if (funcs.getFuncno() != 1 && funcs.getFuncno() != 2){
                allFuncKey.add(funcs.getFuncno());
            }
        }

        //把所有功能的funcno 去移除掉  該角色擁有的funcno = 該角色沒有的funcno
        Set<Integer> missFuncs = new HashSet<>(allFuncKey);
        missFuncs.removeAll(roleFuncKey);

        //透過該角色沒有的funcno 找到funcname,並存入nofuncMap裡
        Map<Integer, String> nofuncMap = new HashMap<>();
        for(Integer funcno : missFuncs){
            nofuncMap.put(funcno, functionRepository.findByFuncno(funcno).getFuncname());
        }
        //存入roleDTO
        roleDTO.setNofuncs(nofuncMap);

        return roleDTO;
    }

    //更改後台成員的大頭貼(上傳更換),用MultipartFile
    public Backuser updateBaImg(BackuserImgDTO backuserImgDTO) throws IOException {
        Optional<Backuser> backuser = Optional.ofNullable(backuserRepository.findByBaaccount(backuserImgDTO.getBaaccount()));
        if (backuser.isPresent()){
            Backuser updateTheBackuser = backuser.get();
            updateTheBackuser.setBaimg(backuserImgDTO.getFile().getBytes());
            return  backuserRepository.save(updateTheBackuser);
        } else {
            throw new RuntimeException("Backuser not found with baaccount: " + backuserImgDTO.getBaaccount());
        }
    }


    //個人資料頁面更改密碼
    public Map<String, String> updateBapassword(String baaccount, String oldPassword, String newPassword, String confirmPassword){
        Map<String, String> resultMap = new HashMap<>();
        Optional<Backuser> backuser = Optional.ofNullable(backuserRepository.findByBaaccount(baaccount));
        if(backuser.isPresent()){
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,12}$";
            Backuser updateTheBackuser = backuser.get();
            boolean oldPasswordMatches = passwordMatchesAndUpgrade(updateTheBackuser, oldPassword);
            //舊密碼與輸入的舊密碼相同 && 新密碼與確認密碼相同 && 新密碼符合正規表式
            if(oldPasswordMatches && newPassword.equals(confirmPassword)&& newPassword.matches(passwordRegex)){
                updateTheBackuser.setBapassword(bcryptPasswordEncoder.encode(newPassword));
                backuserRepository.save(updateTheBackuser);
                System.out.println("確認密碼完成存入新密碼!");
                resultMap.put("success","更改成功");
                return resultMap;
            }
            //新密碼不符合正規表達式
            if (!newPassword.matches(passwordRegex)) {
                resultMap.put("pwdFormatError", "密碼至少包含一個大寫和小寫字母，長度為8~12字元!");
                System.out.println("新密碼格式不符合規定!");
            }
            //舊密碼與輸入的舊密碼不相同
            if (!oldPasswordMatches) {
                resultMap.put("oPwdError", "請確認您的舊密碼!");
                System.out.println("輸入的舊密碼與真正的舊密碼不同!");
            }
            //新密碼與確認密碼不相同
            if (!newPassword.equals(confirmPassword)) {
                resultMap.put("ChPwdError", "請確認您的新密碼!");
                System.out.println("新密碼與確認密碼不同!");
            }
            return resultMap;
        }else {
            throw new RuntimeException("Backuser not found with Baaccount: " + baaccount);
        }
    }

    //更改個人資料
    public Backuser updateBackuser(BackuserDTO backuserDTO){
        Optional<Backuser> backuser = Optional.ofNullable(backuserRepository.findByBaaccount(backuserDTO.getBaaccount()));
        if(backuser.isPresent()){
            Backuser updateTheBackuser = backuser.get();

            updateTheBackuser.setBaname(backuserDTO.getBaname());
            updateTheBackuser.setBacell(backuserDTO.getBacell());
            updateTheBackuser.setBaemail(backuserDTO.getBaemail());
            return backuserRepository.save(updateTheBackuser);
        }else {
            throw new RuntimeException("Backuser not found with Baaccount: " + backuserDTO.getBaaccount());
        }
    }


    //更改密碼(忘記密碼頁面)
    public Backuser updateBuPwd(String email,String password){
        Optional<Backuser> backuser = Optional.ofNullable(backuserRepository.findByBaemail(email));
        if(backuser.isPresent()){
            Backuser updateTheBackuser = backuser.get();
            updateTheBackuser.setBapassword(bcryptPasswordEncoder.encode(password));
            System.out.println("密碼已修改完成!");
            return  backuserRepository.save(updateTheBackuser);
        }else {
            System.out.println("密碼無法修改!");
            throw new RuntimeException("Backuser not found with email: " + email);
        }
    }

    public Backuser createBackuser(Backuser backuser) {
        backuser.setBapassword(bcryptPasswordEncoder.encode(backuser.getBapassword()));
        return backuserRepository.save(backuser);
    }

    private static final String TIMING_DUMMY_HASH =
            new BCryptPasswordEncoder().encode("ezticket-timing-dummy");

    private boolean passwordMatchesAndUpgrade(Backuser backuser, String rawPassword) {
        String storedPassword = backuser.getBapassword();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }
        if (isBcryptPassword(storedPassword)) {
            return bcryptPasswordEncoder.matches(rawPassword, storedPassword);
        }
        if (storedPassword.equals(rawPassword)) {
            backuser.setBapassword(bcryptPasswordEncoder.encode(rawPassword));
            backuserRepository.save(backuser);
            return true;
        }
        return false;
    }

    private boolean isBcryptPassword(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

}
