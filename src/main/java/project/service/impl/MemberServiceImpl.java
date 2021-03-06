package project.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import project.domain.dto.home.HomeReserveDto;
import project.domain.dto.member.MemberInfoDto;
import project.domain.dto.member.MemberUpdateDto;
import project.domain.dto.member.PersonalInfoDto;
import project.domain.dto.member.ReservedHomeInfoDto;
import project.domain.dto.reserve.ReservationRequestHomeDto;
import project.domain.entity.HelpBoardEntityRepository;
import project.domain.entity.HomeEntityRepository;
import project.domain.entity.HomeImageEntityRepository;
import project.domain.entity.HomeReviewEntity;
import project.domain.entity.HomeReviewEntityRepository;
import project.domain.entity.MemberEntityRepository;
import project.domain.entity.MessageEntity;
import project.domain.entity.MessageEntityRepository;
import project.domain.entity.ReservationEntity;
import project.domain.entity.ReservationEntityRepository;
import project.security.dto.SecurityDto;
import project.service.MemberService;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

	private final MemberEntityRepository memberRepository;
	private final HomeEntityRepository homeRepository;
	private final ReservationEntityRepository reservationRepository;
	private final HomeReviewEntityRepository homeReviewRepository;
	private final MessageEntityRepository messageRepository;
	private final HelpBoardEntityRepository helpBoardRepository;
	
	
	//????????? ??????
	@Transactional
	@Override
	public String memberInfo(Model model, SecurityDto securityDto, int page) {
		
		//???????????? ?????????
		MemberInfoDto memberInfo= memberRepository.findById(securityDto.getUsername()).map(MemberInfoDto::new).orElseThrow();
		model.addAttribute("memberInfo",memberInfo);
		
		
		//????????? ??? ?????????
		Pageable pageable =PageRequest.of(page-1, 2, Direction.DESC, "resNo");
		Page<ReservationEntity> reservationEntity = reservationRepository.findAllByMember_email(memberInfo.getEmail(),pageable);
		List<ReservedHomeInfoDto> reservedHomes= reservationEntity.map(ReservedHomeInfoDto::new).get().collect(Collectors.toList());
		
		if(reservationEntity.isEmpty()) {
			model.addAttribute("none","?????? ?????? ?????? ????????? ????????????.");
		}
		
		
		model.addAttribute("reservedHomes",reservedHomes);
		model.addAttribute("pageTot",reservationEntity.getTotalPages());
		
		return "member/member-info";
	}
	
	//???????????? ??????
	@Transactional
	@Override
	public void memberUpdate(MemberUpdateDto updateDto ,Model model) {
		memberRepository.findById(updateDto.getEmail()).map(entity->entity.updateMemberInfo(updateDto)).get();
	}
	
	//????????????
	@Override
	public String homeReserve(long hno, SecurityDto securityDto, HomeReserveDto reserveDto, String message) {
		
		//???????????? ????????? ????????? ?????? ????????? ??????
		String guestsStr=reserveDto.getGuestsStr();
		String guestsStr_=guestsStr.replaceAll("[^0-9]", "");
		if(guestsStr_==null || guestsStr_.equals(""))guestsStr_="0";
		int guests = Integer.parseInt(guestsStr_);
		
		//??????
		ReservationEntity entity = ReservationEntity.builder()
						.checkIn(reserveDto.getCheckIn()).checkOut(reserveDto.getCheckOut()).guests(guests)
						.totalPrice(reserveDto.getTotalPrice()).days(reserveDto.getDays()).reserveStatus("ask")
						.member(memberRepository.findById(securityDto.getUsername()).get())
						.home(homeRepository.findById(hno).get())
						.build();
		
		reservationRepository.save(entity);
		
		//??????????????? ????????? ??????
		MessageEntity messageEntity=MessageEntity.builder()
				.sender(memberRepository.findById(securityDto.getUsername()).get())
				.receiver(homeRepository.findById(hno).get().getMember())
				.message(message)
				.build();
		
		messageRepository.save(messageEntity);
		
		return "redirect:/member/info";
	}
	
	//?????? ?????? ?????????
	@Override
	public String homeReserveRequest(long hno, HomeReserveDto reserveDto, Model model) {
		ReservationRequestHomeDto homesInfo= homeRepository.findById(hno).map(ReservationRequestHomeDto::new).orElseThrow();
		model.addAttribute("homesInfo", homesInfo);
		model.addAttribute("reservationsInfo", reserveDto);
		return "home/reservation-request";
	}
	
	//?????? ??????
	@Override
	public void reviewWrite(long resNo, long hno,SecurityDto securityDto, String review) {
		HomeReviewEntity homeReviewEntity = HomeReviewEntity.builder().review(review)
				.member(memberRepository.findById(securityDto.getUsername()).get())
				.home(homeRepository.findById(hno).get()).reservation(reservationRepository.findById(resNo).get())		
				.build();
		
		homeReviewRepository.save(homeReviewEntity);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	//????????????
	@Override
	public String personalInfoPage(SecurityDto securityDto, Model model) {
		//???????????? ?????????
		PersonalInfoDto memberInfo= memberRepository.findById(securityDto.getUsername()).map(PersonalInfoDto::new).orElseThrow();
		model.addAttribute("memberInfo",memberInfo);
		return "member/personal-info";
	}
	
	//????????? ????????? ?????????
	@Transactional
	@Override
	public String photoUpload(MultipartFile fileImg,SecurityDto securityDto) {
		
		String photoName=securityDto.getUsername()+"_photo.jpg";
		String path="/image/member/";
		
		ClassPathResource cpr = new ClassPathResource("static"+path);
		
		try {
			File location=cpr.getFile();
			fileImg.transferTo(new File(location,photoName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		memberRepository.findById(securityDto.getUsername()).map(e->e.updatePhotoName(photoName));
		
		return path+photoName;
		
	}
	
	//????????? ?????? ?????????
	@Override
	public String memberPhotoPage(Model model,SecurityDto securityDto) {
		String photoName= memberRepository.findById(securityDto.getUsername()).get().getPhotoName();
		model.addAttribute("photo", photoName);
		return "member/member-photo";
	}
	//?????? ??????
	@Override
	public void memberDelete(SecurityDto securityDto) {
		
		String email=securityDto.getUsername();
		
		homeReviewRepository.findByMember_email(email).forEach(e->homeReviewRepository.deleteById(e.getRno()));
		reservationRepository.findByMember_email(email).forEach(e->reservationRepository.deleteById(e.getResNo()));
		homeRepository.findByMember_email(email).forEach(e->homeRepository.deleteById(e.getHno()));
		messageRepository.findBySender_emailOrReceiver_email(email,email).forEach(e->messageRepository.deleteById(e.getMno()));
		helpBoardRepository.findByMember_email(email).forEach(e->helpBoardRepository.deleteById(e.getBno()));
		memberRepository.deleteById(email);
		
		SecurityContextHolder.clearContext();
	}

}
