package com.aline.usermicroservice.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.annotation.security.PermitAll;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import com.aline.core.dto.request.UserAvatarRequest;
import com.aline.core.model.user.UserAvatar;
import com.aline.core.repository.ApplicantRepository;
import com.aline.core.repository.AvatarRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvatarService {

	private final AvatarRepository avatarRepository;

	@PreAuthorize("@authService.canAccess(#id)")
	public void putAvatar(long id, UserAvatarRequest image) {
		try {
			String base64 = image.getPic().split(",")[1];
			byte[] imageArray = Base64.getDecoder().decode(base64);
			UserAvatar blackImage = new UserAvatar(id, imageArray);
			avatarRepository.save(blackImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PermitAll
	public UserAvatarRequest getAvatar(long id) {
		UserAvatar imageModel = avatarRepository.findById(id).orElseThrow();
		String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageModel.getPic());
		UserAvatarRequest image = new UserAvatarRequest(base64);
		return image;
	}
}