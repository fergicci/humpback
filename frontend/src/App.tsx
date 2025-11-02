import { Route, BrowserRouter as Router, Routes } from "react-router-dom";

import { AuthProvider } from "@/auth/AuthProvider";
import { ProtectedRoute, RequireRoles } from "@/auth/ProtectedRoute";

import Footer from "@/components/Footer";
import Header from "@/components/Header";

import Booking from "@/pages/Booking";
import Contact from "@/pages/Contact";
import Gallery from "@/pages/Gallery";
import Gear from "@/pages/Gear";
import Home from "@/pages/Home";
import NotFound from "@/pages/NotFound";
import Contacts from "@/pages/Contacts";
import Login from "@/pages/Login";

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
              <Route path="/login" element={<Login />} />
              <Route element={<ProtectedRoute />}>
                <Route element={<RequireRoles roles={["ADMIN"]} />}>
                  <Route path="/admin/contacts" element={<Contacts />} />
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
