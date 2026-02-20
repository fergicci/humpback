import { Route, BrowserRouter as Router, Routes } from "react-router-dom";

import { AuthProvider } from "@/auth/AuthProvider";
import { ProtectedRoute } from "@/auth/ProtectedRoute";

import Footer from "@/components/Footer";
import Header from "@/components/Header";

import Booking from "@/pages/Booking";
import Contact from "@/pages/Contact";
import Gallery from "@/pages/Gallery";
import Gear from "@/pages/Gear";
import Home from "@/pages/Home";
import NotFound from "@/pages/NotFound";
import Login from "@/pages/Login";
import Register from "@/pages/Register";

import AdminLayout from "@/layouts/AdminLayout";
import Dashboard from "@/pages/admin/Dashboard";
import Contacts from "@/pages/admin/Contacts";
import Bookings from "@/pages/admin/Bookings";
import Users from "@/pages/admin/Users";

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="d-flex flex-column min-vh-100">
          <Header />

          <main className="flex-fill">
            <Routes>
              {/* Public */}
              <Route path="/" element={<Home />} />
              <Route path="/gear" element={<Gear />} />
              <Route path="/gallery" element={<Gallery />} />
              <Route path="/booking" element={<Booking />} />
              <Route path="/contact" element={<Contact />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* Admin (only requires authentication) */}
              <Route element={<ProtectedRoute />}>
                <Route path="/admin" element={<AdminLayout />}>
                  <Route index element={<Dashboard />} />
                  <Route path="dashboard" element={<Dashboard />} />
                  <Route path="contacts" element={<Contacts />} />
                  <Route path="bookings" element={<Bookings />} />
                  <Route path="users" element={<Users />} />
                </Route>
              </Route>

              <Route path="*" element={<NotFound />} />
            </Routes>
          </main>

          <Footer />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;