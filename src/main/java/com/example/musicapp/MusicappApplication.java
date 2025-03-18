package com.example.musicapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
public class MusicappApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicappApplication.class, args);

//		var token = "y0__xDQ_ZX5ARil3zUgsZeLuRK7jpbqJFKwzA6ZRtNyRckI_TNkuA";
//		var client_id = "d4dcb0c7662845e18bb9c9d879b6d8e6";
//		HttpClient client = HttpClient.newHttpClient();
//		HttpRequest request = HttpRequest.newBuilder()
//				.uri(URI.create("https://login.yandex.ru/info?&oauth_token=" + token + "&json"))
//				.GET() // Или же .POST(), .PUT() и т.д., в зависимости от задачи
//				.build();
//		HttpResponse<String> response;
//
//		{
//			try {
//				response = client.send(request, HttpResponse.BodyHandlers.ofString());
//			} catch (IOException | InterruptedException e) {
//				throw new RuntimeException(e);
//			}
//		}
//		System.out.println(response.body());
	}
}
