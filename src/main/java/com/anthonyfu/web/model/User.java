package com.anthonyfu.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NonNull
@ToString
@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USR_ID")
	private String usrId; // 會員ID

	@Column(name = "USR_NAME")
	private String usrName; // 姓名

	@Column(name = "USR_ACCOUNT")
	private String usrAccount; // 帳號

	@Column(name = "USR_PASSWORD")
	private String usrPassword; // 密碼

}
