import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useWebSocket = (url: string = '/metrics/current') => {
    const clientRef = useRef<Client | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS(url),
            onConnect: () => {
                console.log('WebSocket connected');
                setIsConnected(true);
                setError(null);
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                setError('WebSocket error');
            },
            reconnectDelay: 5000,
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [url]);

    const subscribe = useCallback(
        (destination: string, callback: (data: any) => void) => {
            if (clientRef.current && clientRef.current.connected) {
                return clientRef.current.subscribe(destination, (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        callback(data);
                    } catch (e) {
                        console.error('Failed to parse message', e);
                    }
                });
            }
            return null;
        },
        []
    );

    return {
        isConnected,
        error,
        subscribe,
    };
};