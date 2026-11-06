package com.universidad.control_asistencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ControlAsistenciaApplication {

	public static void main(String[] args) {
		// Hora de Colombia en cualquier servidor (Render usa UTC): las fechas se guardan y muestran bien
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
		SpringApplication.run(ControlAsistenciaApplication.class, args);
	}

}
//este es