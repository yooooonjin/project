package project.service.impl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import project.domain.dto.home.HomeDetailListDto;
import project.domain.dto.home.HomeImgRegDto;
import project.domain.dto.home.HomeListDto;
import project.domain.dto.home.HomeRegDto;
import project.domain.dto.home.HomeReviewDto;
import project.domain.dto.home.HomeSearchDto;
import project.domain.entity.HomeEntity;
import project.domain.entity.HomeEntityRepository;
import project.domain.entity.HomeImageEntity;
import project.domain.entity.HomeImageEntityRepository;
import project.domain.entity.HomeOption;
import project.domain.entity.HomeReviewEntity;
import project.domain.entity.HomeReviewEntityRepository;
import project.domain.entity.MemberEntityRepository;
import project.domain.entity.MemberRole;
import project.domain.entity.ReservationEntity;
import project.domain.entity.ReservationEntityRepository;
import project.security.dto.SecurityDto;
import project.service.HomeService;
import project.util.PageInfo;

@Log4j2
@RequiredArgsConstructor
@Service
public class HomeServiceImpl implements HomeService {
	
	final HomeEntityRepository homeRepository;
	final MemberEntityRepository memberRepository;
	final HomeReviewEntityRepository reviewRepository;
	final ReservationEntityRepository reservationRepository;
	final HomeImageEntityRepository homeImageRepository;
	
	//??? ?????? ???????????? //???????????????
	@Override
	public String homeList(Model model,int page) {
		
		//??? ???????????? ??? 3??????
		Pageable pageable =PageRequest.of(page-1, 3, Direction.DESC, "hno");
		Page<HomeEntity> homesEntity =homeRepository.findAll(pageable);
		List<HomeListDto> homes =homesEntity.stream().map(HomeListDto::new).collect(Collectors.toList());
		model.addAttribute("homes",homes);
		
		//????????? 3??????
		PageInfo paging=new PageInfo(homesEntity.getTotalPages(),page,3);
		model.addAttribute("paging",paging);
		return "home/homes";
	}
	
	//????????? ?????? ??? ????????????
	@Transactional
	@Override
	public String homesearch(Model model, int page, HomeSearchDto searchDto) {
		
		//?????? ?????? ??? ???????????? ????????????
		String[] locationArr= searchDto.getLocation().split("[ ]");
		log.info("?????????={}",locationArr[1]);
		String location=locationArr[1];
		
		//???????????? ????????? ????????? ?????? ????????? ??????
		String guestsStr=searchDto.getGuestsStr();
		String guestsStr_=guestsStr.replaceAll("[^0-9]", "");
		if(guestsStr_==null || guestsStr_.equals(""))guestsStr_="0";
		int guests = Integer.parseInt(guestsStr_);
		
		//?????? ?????? & ???????????? ????????? ?????? ???
		List<Long> hno = homeRepository.selectHome(location,guests);
		
		//?????? ????????? ?????? ????????? ???
		LocalDate checkin= searchDto.getCheckin();
		LocalDate checkout=searchDto.getCheckout();
		
		List<HomeEntity> homeEntity=null;
		if(checkin!=null && checkout!=null) { //?????????, ???????????? ????????? ??????????????? ??????
			homeEntity= homeRepository.findAllById(hno).stream() 
				.filter(e->e.isreservations( checkin, checkout)) //????????? ??? ??????????????? ????????? ?????? ????????? ?????? ?????????
				.collect(Collectors.toList());
		}else { //?????????, ???????????? ????????? ???????????? ????????? ??????
			LocalDate today=LocalDate.now();
			homeEntity= homeRepository.findAllById(hno).stream()
					.filter(e->e.isreservations( today.plusDays(1), today.minusDays(1)))
					.collect(Collectors.toList()); //?????? ???????????? ??????,??????????????? ???????????? ???
		}
		
		List<HomeListDto> homes= homeEntity.stream().map(HomeListDto::new).collect(Collectors.toList());
		
		model.addAttribute("homes", homes);
		model.addAttribute("searchDto", searchDto);
		return "home/homes";
		
	}
	
	//??? ????????? ?????????
		@Transactional
		@Override
		public String homeDetail(Model model, long hno, HomeSearchDto searchDto) {
			
			System.out.println("????????? : " +searchDto.getCheckin());
			
			//??? ?????? ????????????
			HomeDetailListDto homeDetails= homeRepository.findById(hno).map(HomeDetailListDto::new).orElseThrow();
			System.out.println("??????????????? : "+homeDetails.getName());
			model.addAttribute("homeDetails", homeDetails);
			
		
			//?????????????????? ????????? reservationDates Set???????????? ??????
			List<ReservationEntity> reservationEntity= reservationRepository.findByHome_hno(hno);
			
			Set<LocalDate> reservationDates = new HashSet<LocalDate>();
			
			reservationEntity.forEach(e->{
				LocalDate checkin= e.getCheckIn();
				LocalDate checkout= e.getCheckOut();
				checkin.datesUntil(checkout).forEach(date-> reservationDates.add(date));
			});
			model.addAttribute("reservationDates", reservationDates);

			
			//?????? ????????? ??????
			List<HomeReviewDto> homeReview = reviewRepository.findAllByHome_hno(hno).stream().map(HomeReviewDto::new).collect(Collectors.toList());
			if(homeReview.isEmpty()) model.addAttribute("none", "????????? ???????????? ????????????.");			
			model.addAttribute("homeReviews", homeReview);
			
			return "home/home-detail";
		}
		
		//???????????? temp ?????? ?????????
		@Override
		public String tempImgUpload(MultipartFile fileImg, String imageName) {
			String path="/image/temp/";
			
			String fileName=imageName+".jpg";
			ClassPathResource cpr=new ClassPathResource("static"+path);
			try {
				File tempLocation=cpr.getFile();
				fileImg.transferTo(new File(tempLocation, fileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return path+fileName;
		}
		
		//??? ??????
		@Transactional
		@Override
		public String homeReg(HomeRegDto regDto, SecurityDto securityDto,HomeImgRegDto imgRegDto) {
			HomeEntity entity= HomeEntity.builder().homeName(regDto.getHomeName()).homeAddress(regDto.getHomeAddress())
				.homePrice(regDto.getHomePrice()).maximumNumber(regDto.getMaximumNumber()).bedNumber(regDto.getBedNumber())
				.bedroomNumber(regDto.getBedroomNumber()).bathroomNumber(regDto.getBathroomNumber())
				.homeType(regDto.getHomeType())
				//.homeOptionSet(null)
				.homeIntro(regDto.getHomeIntro())
				.useable("y")
				.member(memberRepository.findById(securityDto.getUsername()).get())
				.build();
			
			HomeOption[] homeOption = regDto.getHomeOption();
			
			for(int i=0;i<homeOption.length;i++) {
				entity.addHomeOption(homeOption[i]);
			}
			homeRepository.save(entity);
			memberRepository.findById(securityDto.getUsername()).get().addRole(MemberRole.HOST);
			
			///////////////////////////////////////////////////////////////////////
			ClassPathResource tempCpr=new ClassPathResource("static/image/temp/");//????????????????????????
			ClassPathResource destCpr=new ClassPathResource("static/image/home-img");//???????????????????????????
			try {
				File tempLocation=tempCpr.getFile();
				for(File file : tempLocation.listFiles()) {
					
					File tempFile=new File(tempCpr.getFile(), file.getName());
					tempFile.renameTo(new File(destCpr.getFile(), entity.getHno()+"_"+file.getName()));
					
					tempFile.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			///////////////////////////////////////////////////////////////////////
			
				
			HomeImageEntity mainEntity = HomeImageEntity.builder().fileOrgName(imgRegDto.getMainImg().getOriginalFilename())
							.fileNewName(entity.getHno()+"_main-img.jpg").fileSize(imgRegDto.getMainImg().getSize())
							.orderNo(1).home(homeRepository.findById(entity.getHno()).get()).build();
			
			HomeImageEntity sub1Entity = HomeImageEntity.builder().fileOrgName(imgRegDto.getSub1Img().getOriginalFilename())
							.fileNewName(entity.getHno()+"_sub1-img.jpg").fileSize(imgRegDto.getSub1Img().getSize())
							.orderNo(2).home(homeRepository.findById(entity.getHno()).get()).build();
			
			HomeImageEntity sub2Entity = HomeImageEntity.builder().fileOrgName(imgRegDto.getSub2Img().getOriginalFilename())
							.fileNewName(entity.getHno()+"_sub2-img.jpg").fileSize(imgRegDto.getSub2Img().getSize())
							.orderNo(3).home(homeRepository.findById(entity.getHno()).get()).build();
			
			HomeImageEntity sub3Entity = HomeImageEntity.builder().fileOrgName(imgRegDto.getSub3Img().getOriginalFilename())
							.fileNewName(entity.getHno()+"_sub3-img.jpg").fileSize(imgRegDto.getSub3Img().getSize())
							.orderNo(4).home(homeRepository.findById(entity.getHno()).get()).build();
			
			HomeImageEntity sub4Entity = HomeImageEntity.builder().fileOrgName(imgRegDto.getSub4Img().getOriginalFilename())
							.fileNewName(entity.getHno()+"_sub4-img.jpg").fileSize(imgRegDto.getSub4Img().getSize())
							.orderNo(5).home(homeRepository.findById(entity.getHno()).get()).build();
			
			homeImageRepository.save(mainEntity);
			homeImageRepository.save(sub1Entity);
			homeImageRepository.save(sub2Entity);
			homeImageRepository.save(sub3Entity);
			homeImageRepository.save(sub4Entity);
			
			///////////////////////////////////////////////////////////////////////
			
			return "redirect:/home/list";
		}


}
