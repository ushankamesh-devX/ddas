import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "DDAS Admin",
  description: "Dam monitoring and emergency operations console",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="flex min-h-full flex-col">{children}</body>
    </html>
  );
}
