import React, { useEffect, useRef } from 'react';
import Chart from 'chart.js/auto';
import { CityMetrics } from '../types/metrics';

interface TopCitiesChartProps {
    data: CityMetrics[];
}

export const TopCitiesChart: React.FC<TopCitiesChartProps> = ({ data }) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const chartRef = useRef<Chart | null>(null);

    useEffect(() => {
        if (!canvasRef.current || !data || data.length === 0) return;

        const ctx = canvasRef.current.getContext('2d');
        if (!ctx) return;

        if (chartRef.current) {
            chartRef.current.destroy();
        }

        chartRef.current = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map((city) => city.city),
                datasets: [
                    {
                        label: 'Active Rides',
                        data: data.map((city) => city.activeRides),
                        backgroundColor: [
                            '#FF6384',
                            '#36A2EB',
                            '#FFCE56',
                            '#4BC0C0',
                            '#9966FF',
                        ],
                        borderColor: '#fff',
                        borderWidth: 1,
                    },
                ],
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                indexAxis: 'y',
                plugins: {
                    legend: {
                        display: false,
                    },
                    title: {
                        display: true,
                        text: 'Top 5 Active Cities',
                        font: { size: 16, weight: 'bold' },
                    },
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                        },
                    },
                },
            },
        });

        return () => {
            if (chartRef.current) {
                chartRef.current.destroy();
            }
        };
    }, [data]);

    return (
        <div className="chart-container">
            <canvas ref={canvasRef}></canvas>
        </div>
    );
};