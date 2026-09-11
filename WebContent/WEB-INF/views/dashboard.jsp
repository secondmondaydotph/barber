<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Barber | Dashboard</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Favicon -->
    <link href="${pageContext.request.contextPath}/assets/dashboard/img/user.jpg" rel="icon">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Heebo:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Icon Font Stylesheet -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.10.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="${pageContext.request.contextPath}/assets/dashboard/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/dashboard/lib/tempusdominus/css/tempusdominus-bootstrap-4.min.css" rel="stylesheet" />

    <!-- Customized Bootstrap Stylesheet -->
    <link href="${pageContext.request.contextPath}/assets/dashboard/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="${pageContext.request.contextPath}/assets/dashboard/css/style.css" rel="stylesheet">
</head>

<body>
    <div class="container-fluid position-relative bg-white d-flex p-0">
        <!-- Spinner Start -->
        <div id="spinner" class="show bg-white position-fixed translate-middle w-100 vh-100 top-50 start-50 d-flex align-items-center justify-content-center">
            <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status">
                <span class="sr-only">Loading...</span>
            </div>
        </div>
        <!-- Spinner End -->


        <!-- Sidebar Start -->
        <div class="sidebar pe-4 pb-3">
            <nav class="navbar bg-light navbar-light">
                <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand mx-4 mb-3">
                    <h3 class="text-primary"><i class="fa fa-hashtag me-2"></i>Barber</h3>
                </a>
                <div class="d-flex align-items-center ms-4 mb-4">
                    <div class="position-relative">
                        <img class="rounded-circle" src="${pageContext.request.contextPath}/assets/dashboard/img/user.jpg" alt="" style="width: 40px; height: 40px;">
                        <div class="bg-success rounded-circle border border-2 border-white position-absolute end-0 bottom-0 p-1"></div>
                    </div>
                    <div class="ms-3">
                        <h6 class="mb-0"><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).name())%></h6>
                        <span><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).role())%></span>
                    </div>
                </div>
                <div class="navbar-nav w-100">
<a href="${pageContext.request.contextPath}/dashboard" class="nav-item nav-link"><i class="fa fa-tachometer-alt me-2"></i>Dashboard</a>
<a href="${pageContext.request.contextPath}/staff/bookings" class="nav-item nav-link"><i class="fa fa-calendar me-2"></i>Appointments</a>
<% if(((com.barber.model.User)session.getAttribute("user")).role().equals("ADMIN")){ %>
<a href="${pageContext.request.contextPath}/admin/customers" class="nav-item nav-link"><i class="fa fa-users me-2"></i>Customers</a>
<a href="${pageContext.request.contextPath}/admin/barbers" class="nav-item nav-link"><i class="fa fa-user me-2"></i>Barbers</a>
<a href="${pageContext.request.contextPath}/admin/services" class="nav-item nav-link"><i class="fa fa-cut me-2"></i>Services</a>
<a href="${pageContext.request.contextPath}/admin/addons" class="nav-item nav-link"><i class="fa fa-plus me-2"></i>Add-ons</a>
<a href="${pageContext.request.contextPath}/admin/schedules" class="nav-item nav-link"><i class="fa fa-clock me-2"></i>Schedules</a>
<% } %><a href="${pageContext.request.contextPath}/home" class="nav-item nav-link"><i class="fa fa-home me-2"></i>Public site</a>
</div></nav>
        </div>
        <!-- Sidebar End -->


        <!-- Content Start -->
        <div class="content">
            <!-- Navbar Start -->
            <nav class="navbar navbar-expand bg-light navbar-light sticky-top px-4 py-0">
                <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand d-flex d-lg-none me-4">
                    <h2 class="text-primary mb-0"><i class="fa fa-hashtag"></i></h2>
                </a>
                <a href="#" class="sidebar-toggler flex-shrink-0">
                    <i class="fa fa-bars"></i>
                </a>
                <span class="d-none d-md-flex ms-4">Barber management</span>
                <div class="navbar-nav align-items-center ms-auto"><div class="nav-item dropdown"><a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown"><img class="rounded-circle me-lg-2" src="${pageContext.request.contextPath}/assets/dashboard/img/user.jpg" alt="Profile" style="width:40px;height:40px"><span class="d-none d-lg-inline-flex"><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).name())%></span></a><div class="dropdown-menu dropdown-menu-end bg-light border-0 rounded-0 rounded-bottom m-0"><span class="dropdown-item-text"><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).email())%></span><form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="csrf" value="<%=com.barber.util.Html.e(session.getAttribute("csrf"))%>"><button class="dropdown-item">Sign out</button></form></div></div></div></nav>
            <!-- Navbar End -->


            <%@ include file="dashboard-content.jspf" %>


            <!-- Footer Start -->
            <div class="container-fluid pt-4 px-4">
                <div class="bg-light rounded-top p-4">
                    <div class="row">
                        <div class="col-12 col-sm-6 text-center text-sm-start">
                            &copy; <a href="#">Barber</a>, All Right Reserved. 
                        </div>
                        <div class="col-12 col-sm-6 text-center text-sm-end">
                            <!--/*** This template is free as long as you keep the footer author’s credit link/attribution link/backlink. If you'd like to use the template without the footer author’s credit link/attribution link/backlink, you can purchase the Credit Removal License from "https://htmlcodex.com/credit-removal". Thank you for your support. ***/-->
                            Designed By <a href="https://htmlcodex.com">HTML Codex</a>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Footer End -->
        </div>
        <!-- Content End -->


        <!-- Back to Top -->
        <a href="#" class="btn btn-lg btn-primary btn-lg-square back-to-top"><i class="bi bi-arrow-up"></i></a>
    </div>

    <!-- JavaScript Libraries -->
    <script src="${pageContext.request.contextPath}/assets/vendor/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/vendor/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/chart/chart.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/easing/easing.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/waypoints/waypoints.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/owlcarousel/owl.carousel.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/tempusdominus/js/moment.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/tempusdominus/js/moment-timezone.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/dashboard/lib/tempusdominus/js/tempusdominus-bootstrap-4.min.js"></script>

    <!-- Template Javascript -->
    <script src="${pageContext.request.contextPath}/assets/dashboard/js/main.js"></script>
<script src="${pageContext.request.contextPath}/assets/barber.js"></script></body>

</html>