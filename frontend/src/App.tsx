import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import Home from "@/pages/Home";
import Gear from "@/pages/Gear";
import Gallery from "@/pages/Gallery";
import Booking from "@/pages/Booking";
import Contact from "@/pages/Contact";
import NotFound from "@/pages/NotFound";
import { AuthProvider } from "@/auth/AuthProvider";
import { ProtectedRoute, RequireRoles } from "@/auth/ProtectedRoute";

import LoginPage from "@/pages/LoginPage";
import ContactsPage from "@/pages/ContactsPage";

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="d-flex flex-column min-vh-100">
          <Header />
          <main className="flex-fill">
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/gear" element={<Gear />} />
              <Route path="/gallery" element={<Gallery />} />
              <Route path="/booking" element={<Booking />} />
              <Route path="/contact" element={<Contact />} />
              <Route path="/login" element={<LoginPage />} />
              <Route element={<ProtectedRoute />}>
                <Route element={<RequireRoles roles={["ADMIN"]} />}>
                  <Route path="/admin/contacts" element={<ContactsPage />} />
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