package project.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import project.security.dto.SecurityDto;
import project.service.MessageService;
import project.service.impl.MessageServiceImpl;

@RequiredArgsConstructor
@RequestMapping("/member/message")
@Controller
public class MessageController {
	
	private final MessageService service;
	
	//메세지 페이지
	@GetMapping
	public String messagePage(@AuthenticationPrincipal SecurityDto securityDto, Model model) {
		return service.messagePage(securityDto,model);
	}
	//메세지 내용
	@GetMapping("/detail")
	public String messageDetail(@AuthenticationPrincipal SecurityDto securityDto, Model model, String targetEmail) {
		return service.messageDetail(securityDto,model,targetEmail);
	}
	
	//메세지 디테일 페이지에서 메세지 전송
	@ResponseBody
	@PostMapping("/detail")
	public void messageDetailwrite(@AuthenticationPrincipal SecurityDto securityDto, Model model, String targetId, String message) {
		service.messageDetailWrite(securityDto,model,targetId,message);
	}

}
