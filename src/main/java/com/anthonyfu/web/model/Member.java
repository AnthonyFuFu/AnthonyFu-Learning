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
@Table(name = "member", schema = "anthonyfu_learning")
@NoArgsConstructor
@AllArgsConstructor
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MEM_ID", length = 255)
	private String memId; // 會員ID

	@Column(name = "MEM_NAME", length = 45)
	private String memName; // 姓名

	@Column(name = "MEM_ACCOUNT", length = 50)
	private String memAccount; // 帳號

	@Column(name = "MEM_PASSWORD", length = 20)
	private String memPassword; // 密碼

}
