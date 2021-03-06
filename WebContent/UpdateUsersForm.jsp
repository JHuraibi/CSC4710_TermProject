<%@ page
		language="java" contentType="text/html; charset=UTF-8"
		pageEncoding="UTF-8"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%------------------------------------------%>
<%--|	  FORMERLY: UserForm.jsp         |--%>
<%--|  Split into New and Update Forms   |--%>
<%------------------------------------------%>

<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="stylesheet.css">
	<title>Edit My Account</title>
</head>
<body>
	<%
		// Verify a user logged in
		if (session == null) {
			response.sendRedirect("login.jsp");
		}
	%>
	<div id="wrapper">
		<header>
			<h1>Edit Your Account Information</h1>
		</header>
		<nav>
			<ul>
				<li><a href="index.jsp">Home</a></li>
				<li><a href="PostAnimal">Put an Animal Up for Adoption</a></li>
				<li><a href="ListAnimals">All Animals</a></li>
				<li><a href="ListBreeders">All Breeders</a></li>
				<li><a href="SearchByTrait.jsp">Search for an Animal</a></li>
				<li><a href="LogoutUser">Log Out</a></li>
			</ul>
		</nav>
		<main>
			<h2><c:out value="${sessionScope.sUsername}"/></h2>
			<form action="SubmitUpdate" method="post" autofocus="autofocus">
				<!-- The UPDATE query needs the original username -->
				<input type="text" name="username" value="<c:out value='${currentUser.username}' />"/>
				
				<c:if test="${currentUser.username != root}">
					<!-- root user(s) cannot change their username -->
					<label for="newUsername">Username</label>
					<input
							type="text" name="username"
							id="newUsername" size="45" required
							value="${currentUser.username}"
					/>
				</c:if>
				
				<!-- IF TIME: Implement a "passwords must match" feature -->
				<label for="newPass">Password</label>
				<input
						type="password" name="password"
						id="newPass" size="45" required
						value="${currentUser.password}"
				/>
				<label for="newFName">First Name: </label>
				<input
						type="text" name="firstName"
						id="newFName" size="45" required
						value="<c:out value='${currentUser.firstName}' />"
				/>
				
				<label for="newLName">Last Name</label>
				<input
						type="text" name="lastName"
						id="newLName" size="45" required
						value="<c:out value='${currentUser.lastName}' />"
				/>
				<label for="newEmail">Email</label>
				<input
						type="email" name="email"
						id="newEmail" size="45" required
						value="<c:out value='${currentUser.email}' />"
				/>
				<input type="reset" value="Clear all Fields">
				<input type="submit" value="Submit Changes" id="mySubmit"/>
			</form>
			<a href="MyAccount">Return to My Account</a>
		</main>
	</div>
</body>
</html>
