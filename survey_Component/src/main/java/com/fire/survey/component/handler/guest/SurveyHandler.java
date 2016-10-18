package com.fire.survey.component.handler.guest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fire.survey.component.service.i.SurveyService;
import com.fire.survey.entities.guest.Survey;
import com.fire.survey.entities.guest.User;
import com.fire.survey.utils.ConstanName;
import com.fire.survey.utils.DataProcess;
import com.fire.survey.utils.Page;

@Controller
@RequestMapping("/survey")
public class SurveyHandler {
	// 图片压缩
	@Autowired
	private SurveyService surveyService;

	@RequestMapping("/edit/{id}")
	public String edit(@PathVariable("id") Integer id, Map<String, Object> map) {
		Survey survey = surveyService.getSurveyById(id);
		map.put("survey", survey);
		return "guest/edit_survey";
	}

	@RequestMapping("/doDelete/{id}/{pageStr}")
	public String doDelete(@PathVariable("id") Integer id, @PathVariable("pageStr") String str) {
		surveyService.removieSurvey(id);
		return "redirect:/survey/completeSurvey?pageStr=" + str;
	}

	@RequestMapping("/completeSurvey")
	public String showAllSurvey(@RequestParam(value = "pageStr", required = false) String pageNoStr,
			HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(ConstanName.LOGIN_USER);
		Page<Survey> page = surveyService.getMyUnCompleted(user, pageNoStr);
		request.setAttribute("page", page);
		return "guest/complete_survey";
	}

	@RequestMapping("/doAddSurvey")
	public String doAddSurvey(String name, @RequestParam("logoFile") MultipartFile multipartFile,
			HttpServletRequest request) throws RuntimeException, IOException {
		if (multipartFile.isEmpty()) {
			User user = (User) request.getSession().getAttribute(ConstanName.LOGIN_USER);
			surveyService.save(name, null, user);
			return "redirect://survey/completeSurvey";
		}

		boolean b = multipartFile.getContentType().startsWith("image");
		if (!b) {
			request.setAttribute("message", "只支持图片格式！");
			return "guest/new_survey";
		}
		b = multipartFile.getSize() > 102400;
		if (b) {
			request.setAttribute("message", "文件不能超过100kb！");
			return "guest/new_survey";
		}
		User user = (User) request.getSession().getAttribute(ConstanName.LOGIN_USER);
		String path = request.getServletContext().getRealPath("pics");
		String oName = multipartFile.getOriginalFilename();
		String type = oName.substring(oName.lastIndexOf("."));
		String picPath = DataProcess.resizeImages(multipartFile.getInputStream(), path, type);
		surveyService.save(name, picPath, user);
		return "redirect:/survey/completeSurvey";
	}
}
