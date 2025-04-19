import React from 'react';

function Footer() {
  return (
    <footer className="bg-dark text-white text-center py-4 mt-auto">
      <div className="container">
        <p className="mb-1">© {new Date().getFullYear()} Humpback Studio</p>
      </div>
    </footer>
  );
}

export default Footer;