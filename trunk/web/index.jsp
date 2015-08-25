<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib uri="/WEB-INF/tlds/yunmei.tld" prefix="yunmei"%>
<html>
<head>
<yunmei:import ids="base,extends"/>
<style type="text/css">
.footer{
	margin:0 0 0 540px;
}
.stab{
	margin:5 -300 0 540px;
}
	.inp{
		width: 170px;
	}
	.general{
	}
	.tab{
		margin:140 -300 0 560px;
	}
	.gtab{
		margin:0 0 0 0px; 
		float:center;
	}
</style>
<script type="text/javascript">
function logon(){
	var usernameVal = document.getElementById('username').value;
	var passwordVal = document.getElementById('password').value;
	$.forward('sysAuthService.login', usernameVal, passwordVal);
}
function reset(){
	document.getElementById('username').value = "";
	document.getElementById('password').value = "";
	document.getElementById('username').focus();
}
function zym(){
	document.location.href = "http://www.ziyunmei.com";
}
function getFocus(){

	document.getElementById('username').focus();
}
function keyLogon(){
	if(event.keyCode == 13){
		var usernameVal = document.getElementById('username').value;
		var passwordVal = document.getElementById('password').value;
		$.forward('sysAuthService.login', usernameVal, passwordVal);
	}
}
</script>
</head>
<body onload="getFocus()">
<center>
		<table class="gtab" align="center" cellpadding="0" cellspacing="0">
			<tr>
				<td colspan="2" height="234" width="1500" style="background-image: url('images/logon/head.jpg');background-repeat: no-repeat; ">
				</td>
			</tr>
			<tr>
				<td width="236" height="340" style="background-image: url('images/logon/left.jpg');background-repeat: no-repeat; "></td>
				<td width="610" height="340" style="background-image: url('images/logon/center.jpg'); background-repeat: no-repeat;">
						<table border="0" class="tab" id="tab" cellpadding="3" cellspacing="3">
							<tr> 
								<td><font>&nbsp;&nbsp;&nbsp;&nbsp;</font></td>
								<td><input class="inp" type="text" id="username"></td>
							</tr>
							<tr>
								<td><font>&nbsp;&nbsp;&nbsp;&nbsp;</font></td>
								<td><input class="inp" type="password" id="password"></td>
							</tr>
						</table>
						<table class="stab" border="0">
							<tr>
									<td colspan="2" align="center">
										<input type="image" src="images/logon/confirm.png" onclick="logon()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										<input type="image" src="images/logon/re.png" onclick="reset()">
									</td>
							</tr>
						</table>
				</td>
			</tr>
			<tr>
				<td class="footer" colspan="2" height="68" width="1500" style="background-image: url('images/logon/footer.jpg');background-repeat: no-repeat; ">
				</td>
			</tr>
		</table>
</center>
</body>
</html>