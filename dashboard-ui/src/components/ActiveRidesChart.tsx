import React, { useEffect, useRef } from 'react';
import Chart from 'chart.js/auto';

interface ActiveRidesChartProps {
  data: { timestamp: string; activeRides: number }[];
}

export const ActiveRidesChart: React.FC<ActiveRidesChartProps> = ({ data }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const chartRef = useRef<Chart | null>(null);

  useEffect(() => {
    if (!canvasRef.current || !data || data.length === 0) return;
    const ctx = canvasRef.current.getContext('2d');
    if (!ctx) return;
    if (chartRef.current) chartRef.current.destroy();
    chartRef.current = new Chart(ctx, {
      type: 'line',
      data: {
        labels: data.map((d) => new Date(d.timestamp).toLocaleTimeString()),
        datasets: [
          {
            label: 'Active Rides',
            data: data.map((d) => d.activeRides),
            borderColor: '#36A2EB',
            backgroundColor: 'rgba(54,162,235,0.2)',
            fill: true,
            tension: 0.3,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          title: {
            display: true,
            text: 'Active Rides Over Time',
            font: { size: 16, weight: 'bold' },
          },
        },
        scales: {
          y: { beginAtZero: true },
        },
      },
    });
    return () => {
      if (chartRef.current) chartRef.current.destroy();
    };
  }, [data]);

  return (
    <div className="chart-container">
      <canvas ref={canvasRef}></canvas>
    </div>
  );
};