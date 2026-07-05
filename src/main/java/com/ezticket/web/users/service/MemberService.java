package com.ezticket.web.users.service;

import com.ezticket.web.users.dto.MemSignUpDTO;
import com.ezticket.web.users.dto.MemberDTO;
import com.ezticket.web.users.dto.MemberImgDTO;
import com.ezticket.web.users.pojo.Member;
import com.ezticket.web.users.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;


    //拿取所有會員的資料,過濾只要memberDTO的內容
    public List<MemberDTO> getAllMember() {
        return memberRepository.findAll()
                .stream()
                .map(member -> MemberDTO.builder()
                        .memberno(member.getMemberno())
                        .mname(member.getMname())
                        .memail(member.getMemail())
                        .birth(member.getBirth())
                        .gender(member.getGender())
                        .mcell(member.getMcell())
                        .mphone(member.getMphone())
                        .address(member.getAddress())
                        .memberstatus(member.getMemberstatus())
                        .build())
                .collect(Collectors.toList());
    }

    //更新會員資料,只針對DTO的欄位即可
    public Member updateMember(Integer memberno,MemberDTO newMemberDTO){
        Optional<Member> oldMember = memberRepository.findById(memberno);
        if(oldMember.isPresent()){
            Member updateTheMember = oldMember.get();
            updateTheMember.setMname(newMemberDTO.getMname());
            updateTheMember.setMemail(newMemberDTO.getMemail());
            updateTheMember.setBirth(newMemberDTO.getBirth());
            updateTheMember.setGender(newMemberDTO.getGender());
            updateTheMember.setMcell(newMemberDTO.getMcell());
            updateTheMember.setAddress(newMemberDTO.getAddress());
            return  memberRepository.save(updateTheMember);
        }else {
            throw new RuntimeException("Member not found with memberno: " + memberno);
        }
    }

    //toggle會員的權限狀態  1 <-> 2
    public Member updateMemberStatus(Integer memberno){
        Optional<Member> oldMember = memberRepository.findById(memberno);
        if(oldMember.isPresent()){
            Member updateTheMember = oldMember.get();
            if (updateTheMember.getMemberstatus() == 1) {
                updateTheMember.setMemberstatus(2);
            } else if (updateTheMember.getMemberstatus() == 2) {
                updateTheMember.setMemberstatus(1);
            }
            return memberRepository.save(updateTheMember);

        }else {
            throw new RuntimeException("Member not found with memberno: " + memberno);
        }
    }

    //驗證資料庫有無此會員,回傳布林值
    public boolean authenticate(String memail, String mpassword) {
        Member member = memberRepository.findByMemail(memail);
        if (member == null) {
            // 帳號不存在也跑一次雜湊比對，避免以回應時間差列舉有效帳號
            bcryptPasswordEncoder.matches(mpassword != null ? mpassword : "", TIMING_DUMMY_HASH);
            return false;
        }
        return passwordMatchesAndUpgrade(member, mpassword) && member.getMemberstatus() == 1;
    }

    //取得單一會員的所有基本資料
    public Member getMemberInfo(String memail){
        Member member = memberRepository.findByMemail(memail);
        return member;
    }


    //更改會員的大頭貼(上傳更換),用MultipartFile
    public Member updateMemImg(MemberImgDTO memberImgDTO) throws IOException {
        Optional<Member> member = memberRepository.findById(memberImgDTO.getMemberno());
        if(member.isPresent()){
            Member updateTheMember = member.get();
            updateTheMember.setMimg(memberImgDTO.getFile().getBytes());
            return  memberRepository.save(updateTheMember);
        }else {
            throw new RuntimeException("Member not found with memberno: " + memberImgDTO.getMemberno());
        }
    }

    //個人資料頁面更改密碼
    public Map<String, String> updateMpassword(String email, String oldPassword, String newPassword, String confirmPassword){
        Map<String, String> resultMap = new HashMap<>();
        Optional<Member> member = Optional.ofNullable(memberRepository.findByMemail(email));
        if(member.isPresent()){
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,12}$";
            Member updateTheMember = member.get();
            boolean oldPasswordMatches = passwordMatchesAndUpgrade(updateTheMember, oldPassword);
            //舊密碼與輸入的舊密碼相同 && 新密碼與確認密碼相同 && 新密碼符合正規表式
            if(oldPasswordMatches && newPassword.equals(confirmPassword)&& newPassword.matches(passwordRegex)){
                updateTheMember.setMpassword(bcryptPasswordEncoder.encode(newPassword));
                memberRepository.save(updateTheMember);
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
            throw new RuntimeException("Member not found with Memail: " + email);
        }
    }

    //更改備用收件人
    public Map<String, String> updateComrecipient(String email, String comrecipient, String comrephone, String comreaddress){
        Map<String, String> resultMap = new HashMap<>();
        Optional<Member> member = Optional.ofNullable(memberRepository.findByMemail(email));
        if(member.isPresent()){
            String comrephoneRegex = "^09\\d{8}$";
            Member updateTheMember = member.get();
            System.out.println(comrecipient);
            //電話符合手機格式 ,且三個值都不為空值
            if(comrephone.matches(comrephoneRegex) && comrecipient != "" && comrephone != "" && comreaddress != ""){
                updateTheMember.setComrecipient(comrecipient);
                updateTheMember.setComrephone(comrephone);
                updateTheMember.setComreaddress(comreaddress);
                memberRepository.save(updateTheMember);
                resultMap.put("success","更改成功!");
                return resultMap;
            }
            if (comrecipient == ""){
                System.out.println("備用收件人為空值!");
                resultMap.put("cptNullError","請填寫備用收件人!");
            }
            if (comrephone == "" ){
                System.out.println("備用收件人手機為空值!");
                resultMap.put("cphoneNullError","請填寫備用收件人手機!");
                resultMap.remove("phoneFormatError");
            } else if(!comrephone.matches(comrephoneRegex) && comrephone != ""){
                System.out.println("備用收件人電話不符合格式!");
                resultMap.put("phoneFormatError","手機必須為09開頭的10位數字");
            }

            if (comreaddress == ""){
                System.out.println("備用收件人地址為空值!");
                resultMap.put("caddressNullError","請填寫備用收件人地址!");
            }

            System.out.println("電話是空"+resultMap.containsKey("cphoneNullError"));
            System.out.println("電話格式"+resultMap.containsKey("phoneFormatError"));
            return resultMap;

        }else {
            throw new RuntimeException("Member not found with Memail: " + email);
        }
    }

    //更改個人資料
    public Member updateMember(MemberDTO newmemberDTO){
        Optional<Member> member = Optional.ofNullable(memberRepository.findByMemail(newmemberDTO.getMemail()));
        if(member.isPresent()){
            Member updateTheMember = member.get();

            updateTheMember.setMname(newmemberDTO.getMname());
            updateTheMember.setGender(newmemberDTO.getGender());
            updateTheMember.setBirth(newmemberDTO.getBirth());
            updateTheMember.setMcell(newmemberDTO.getMcell());
            updateTheMember.setMphone(newmemberDTO.getMphone());
            updateTheMember.setAddress(newmemberDTO.getAddress());
            return memberRepository.save(updateTheMember);
        }else {
            throw new RuntimeException("Member not found with Memail: " + newmemberDTO.getMemail());
        }
    }

    //更改密碼(忘記密碼頁面)
    public Member updateMemberPwd(String email,String password){
        Optional<Member> oldMember = Optional.ofNullable(memberRepository.findByMemail(email));
        if(oldMember.isPresent()){
            Member updateTheMember = oldMember.get();
            updateTheMember.setMpassword(bcryptPasswordEncoder.encode(password));
            return  memberRepository.save(updateTheMember);
        }else {
            throw new RuntimeException("Member not found with email: " + email);
        }
    }

    public Member insertMember(MemSignUpDTO memSignUpDTO){
        Member member =new Member();
        member.setMname(memSignUpDTO.getMname());
        member.setMcell(memSignUpDTO.getMcell());
        member.setAddress(memSignUpDTO.getAddress());
//        member.setBirth(memSignUpDTO.getBirth());
        member.setMemail(memSignUpDTO.getMemail());
        member.setMpassword(bcryptPasswordEncoder.encode(memSignUpDTO.getMpassword()));
        member.setMemberstatus(1);  //註冊直接啟用
        memberRepository.save(member);
        return member;


    }

    private static final String TIMING_DUMMY_HASH =
            new BCryptPasswordEncoder().encode("ezticket-timing-dummy");

    private boolean passwordMatchesAndUpgrade(Member member, String rawPassword) {
        String storedPassword = member.getMpassword();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }
        if (isBcryptPassword(storedPassword)) {
            return bcryptPasswordEncoder.matches(rawPassword, storedPassword);
        }
        if (storedPassword.equals(rawPassword)) {
            member.setMpassword(bcryptPasswordEncoder.encode(rawPassword));
            memberRepository.save(member);
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
