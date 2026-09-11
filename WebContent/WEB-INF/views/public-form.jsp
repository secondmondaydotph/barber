<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Limpio HoliÄ | Appointments and grooming</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Favicon -->
    <link href="${pageContext.request.contextPath}/assets/public/img/hero-slider-1.jpg" rel="icon">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Dancing+Script&family=Playfair+Display:wght@500&family=Work+Sans&display=swap"
        rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.10.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="${pageContext.request.contextPath}/assets/public/lib/animate/animate.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/public/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="${pageContext.request.contextPath}/assets/public/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="${pageContext.request.contextPath}/assets/public/css/style.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/assets/public/css/barber-theme.css?v=20260905b" rel="stylesheet"></head>

<body class="barber-form-page">
    <!-- Spinner Start -->
    <div id="spinner"
        class="show bg-white position-fixed translate-middle w-100 vh-100 top-50 start-50 d-flex align-items-center justify-content-center">
        <div class="spinner-grow text-primary" style="width: 3rem; height: 3rem;" role="status">
            <span class="sr-only">Loading...</span>
        </div>
    </div>
    <!-- Spinner End -->


    <!-- Navbar Start -->
    <div class="container-fluid bg-light sticky-top p-0">
        <nav class="navbar navbar-expand-lg navbar-light p-0">
            <a href="${pageContext.request.contextPath}/home" class="navbar-brand bg-primary py-4 px-5 me-0">
                <span class="brand-lockup"><img src="${pageContext.request.contextPath}/assets/public/img/brand-logo.png" alt="Limpio HoliÄ logo" width="56" height="56" style="width:56px;height:56px;object-fit:contain"><span>Limpio HoliÄ</span></span>
            </a>
            <button type="button" class="navbar-toggler me-4" data-bs-toggle="collapse"
                data-bs-target="#navbarCollapse">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse p-3" id="navbarCollapse">
                <div class="navbar-nav mx-auto">
                    <a href="${pageContext.request.contextPath}/home" class="nav-item nav-link">Home</a>
                    <a href="${pageContext.request.contextPath}/about" class="nav-item nav-link">About</a>
                    <a href="${pageContext.request.contextPath}/services" class="nav-item nav-link">Service</a>
                    <div class="nav-item dropdown">
                        <a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown">Pages</a>
                        <div class="dropdown-menu bg-light mt-2">
                            <a href="${pageContext.request.contextPath}/team" class="dropdown-item">Our Team</a>
                            <a href="${pageContext.request.contextPath}/testimonials" class="dropdown-item">Testimonial</a>
                            <a href="${pageContext.request.contextPath}/my-bookings" class="dropdown-item">My appointments</a>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/contact" class="nav-item nav-link active">Contact</a>
                </div>
                <div class="d-flex flex-wrap gap-2"><a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/book">Book now</a>
<% if(session.getAttribute("user")==null){ %><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/login">Sign in</a><% }else{ %><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/profile">My account</a><form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="csrf" value="<%=com.barber.util.Html.e(session.getAttribute("csrf"))%>"><button class="btn btn-sm btn-outline-primary">Sign out</button></form><% } %></div>
            </div>
        </nav>
    </div>
    <!-- Navbar End -->


    


    <%@ include file="form-content.jspf" %>
        

    <!-- Footer Start -->
    <div class="container-fluid footer position-relative bg-dark text-white-50 py-5 mt-5 wow fadeIn" data-wow-delay="0.2s">
        <div class="container py-5">
            <div class="row g-5">
                <div class="col-lg-6 pe-lg-5">
                    <a href="${pageContext.request.contextPath}/home" class="navbar-brand">
                        <h1 class="display-5 text-primary mb-0">Limpio HoliÄ</h1>
                    </a>
                    <p>Aliquyam sed elitr elitr erat sed diam ipsum eirmod eos lorem nonumy. Tempor sea ipsum diam  sed clita dolore eos dolores magna erat dolore sed stet justo et dolor.</p>
                    <p class="mb-2"><i class="fa fa-map-marker-alt me-2"></i>245 Esmeralda St, San Andres Bukid, Metro Manila, Philippines</p>
                    <p class="mb-2"><i class="fa fa-phone-alt me-2"></i>+012 345 67890</p>
                    <p><i class="fa fa-envelope me-2"></i>info@example.com</p>
                    <div class="d-flex justify-content-start mt-4">
                        <a class="btn btn-sm-square btn-primary me-3" href="#"><i class="fab fa-twitter"></i></a>
                        <a class="btn btn-sm-square btn-primary me-3" href="#"><i class="fab fa-facebook-f"></i></a>
                        <a class="btn btn-sm-square btn-primary me-3" href="#"><i class="fab fa-linkedin-in"></i></a>
                        <a class="btn btn-sm-square btn-primary me-3" href="#"><i class="fab fa-instagram"></i></a>
                    </div>
                </div>
                <div class="col-lg-6 ps-lg-5">
                    <div class="row g-4">
                        <div class="col-sm-6">
                            <h5 class="text-primary mb-4">Quick Links</h5>
                            <a class="btn btn-link" href="">About Us</a>
                            <a class="btn btn-link" href="">Contact Us</a>
                            <a class="btn btn-link" href="">Our Services</a>
                            <a class="btn btn-link" href="">Terms & Condition</a>
                        </div>
                        <div class="col-sm-6">
                            <h5 class="text-primary mb-4">Popular Links</h5>
                            <a class="btn btn-link" href="">About Us</a>
                            <a class="btn btn-link" href="">Contact Us</a>
                            <a class="btn btn-link" href="">Our Services</a>
                            <a class="btn btn-link" href="">Terms & Condition</a>
                        </div>
                        <div class="col-sm-12">
                            <h5 class="text-primary mb-4">Newsletter</h5>
                            <div class="position-relative w-100 mb-2">
                                <input class="form-control bg-secondary border-0 w-100 ps-4 pe-5" type="text"
                                    placeholder="Newsletter not connected" disabled style="height: 60px;">
                                <button type="button" class="btn shadow-none position-absolute top-0 end-0 mt-2 me-2"><i
                                        class="fa fa-paper-plane text-primary fs-4"></i></button>
                            </div>
                            <p class="mb-0">Diam sed sed dolor stet amet eirmod</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- Footer End -->


    <!-- Copyright Start -->
    <div class="container-fluid bg-dark text-white border-top border-secondary py-4 wow fadeIn" data-wow-delay="0.1s">
        <div class="container">
            <div class="row">
                <div class="col-md-6 text-center text-md-start mb-3 mb-md-0">
                    &copy; <a class="border-bottom" href="#">Barber</a>, All Right Reserved.
                </div>
                <div class="col-md-6 text-center text-md-end">

                    <!--/*** The author’s attribution link must remain intact in the template. ***/-->
                    <!--/*** If you wish to remove this credit link, please purchase the Pro Version . ***/-->
                    Designed By <a class="border-bottom" href="https://htmlcodex.com">HTML Codex</a>
                </div>
            </div>
        </div>
    </div>
    <!-- Copyright End -->


    <!-- Back to Top -->
    <a href="#" class="btn btn-lg btn-primary btn-lg-square back-to-top"><i class="bi bi-arrow-up"></i></a>


    <!-- JavaScript Libraries -->
    <script src="${pageContext.request.contextPath}/assets/vendor/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/vendor/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/public/lib/wow/wow.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/public/lib/easing/easing.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/public/lib/waypoints/waypoints.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/public/lib/counterup/counterup.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/public/lib/owlcarousel/owl.carousel.min.js"></script>

    <!-- Template Javascript -->
    <script src="${pageContext.request.contextPath}/assets/public/js/main.js"></script>
<script src="${pageContext.request.contextPath}/assets/barber.js"></script></body>

</html>