package project.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import project.domain.dto.home.HomeReserveDto;
import project.domain.dto.home.HomeReviewDto;
import project.domain.dto.member.MemberUpdateDto;
import project.security.dto.SecurityDto;
import project.service.MemberService;

@RequiredArgsConstructor
@RequestMapping("/member")
@Controller
public class MemberController {
	
	private final MemberService service;
	
	//프로필 페이지 이동
	@GetMapping("/info")
	public String memberInfo(Model model, @AuthenticationPrincipal SecurityDto securityDto,@RequestParam(defaultValue = "1") int page) {
		return service.memberInfo(model,securityDto,page);
	}
	//프로필 사진 업로드 페이지 이동
	@GetMapping("/info/photo")
	public String memberPhotoPage(Model model,@AuthenticationPrincipal SecurityDto securityDto) {
		return service.memberPhotoPage(model,securityDto);
	}
	//프로필 사진 업로드
	@ResponseBody
	@PostMapping("/info/photo")
	public String memberPhotoUpload(MultipartFile fileImg,@AuthenticationPrincipal SecurityDto securityDto) {
		return service.photoUpload(fileImg,securityDto);
	}
	//회원정보수정
	@ResponseBody
	@PutMapping
	public void memberUpdate(MemberUpdateDto updateDto,Model model) {
		service.memberUpdate(updateDto,model);
	}
	//리뷰 작성
	@ResponseBody
	@PostMapping("/home/review/{hno}/{resNo}")
	public void reviewWritePage(@PathVariable long resNo, @PathVariable long hno, @AuthenticationPrincipal SecurityDto securityDto, String review) {
		service.reviewWrite(resNo,hno, securityDto,review);
	}
	
	
	//예약요청페이지
	@PostMapping("/home/reserve/request/{hno}")
	public String homeReserveRequestPage(@PathVariable long hno, HomeReserveDto reserveDto, Model model) {
		return service.homeReserveRequest(hno,reserveDto,model);
	}
	//숙소예약
	@PostMapping("/home/reserve/{hno}")
	public String homeReserve(@PathVariable long hno, @AuthenticationPrincipal SecurityDto securityDto, HomeReserveDto reserveDto,String message) {
		return service.homeReserve(hno,securityDto,reserveDto,message);
	}
	
	//계정
	@GetMapping("/account")
	public String accountPage(@AuthenticationPrincipal SecurityDto securityDto, Model model) {
		model.addAttribute("user", securityDto);
		return "member/account";
	}
	//개인정보
	@GetMapping("/personalInfo")
	public String personalInfoPage(@AuthenticationPrincipal SecurityDto securityDto, Model model) {
		return service.personalInfoPage(securityDto,model);
	}
	
	//계정삭제
	@ResponseBody
	@DeleteMapping
	public void memberDelete(@AuthenticationPrincipal SecurityDto securityDto) {
		service.memberDelete(securityDto);
	}
	

}
