import React from 'react';
import logo from '../assets/humpback-logo.png';

function Home() {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center text-center" style={{ minHeight: '80vh' }}>
      <img src={logo} alt="Humpback Studio Logo" style={{ width: '600px', marginBottom: '2rem' }} />
      <h3 className="display-6">Welcome to Humpback Studio</h3>
      <p className="lead">Where sound meets soul. Record. Mix. Create.</p>
    </div>
  );
}

export default Home;