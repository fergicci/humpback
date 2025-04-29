import React from "react";

const SkeletonLoader: React.FC<{ width?: string; height?: string }> = ({ width = "100%", height = "20px" }) => {
  return (
    <div
      style={{
        width,
        height,
        backgroundColor: "#e0e0e0",
        borderRadius: "4px",
        animation: "pulse 1.5s infinite ease-in-out",
      }}
    />
  );
};

export { SkeletonLoader };
