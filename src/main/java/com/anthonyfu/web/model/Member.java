package com.anthonyfu.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NonNull
@ToString
@Entity
@Table(name = "member")
@NoArgsConstructor
@AllArgsConstructor
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MEM_ID")
	private String memId; // 會員ID

	@Column(name = "MEM_NAME")
	private String memName; // 姓名

	@Column(name = "MEM_ACCOUNT")
	private String memAccount; // 帳號

	@Column(name = "MEM_PASSWORD")
	private String memPassword; // 密碼

}
