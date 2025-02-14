package com.externship.appointment.Person_storage;

import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity
@Table(name = "PERSON", uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public class Person {

	@Id
	private String email;

	@Column(nullable = false)
	private String password;

	// Constructors
	public Person() {}

	public Person(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
